package ch.fitnesslab.product.application

import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.generated.model.ProductView
import ch.fitnesslab.product.domain.events.ProductCreatedEvent
import ch.fitnesslab.product.domain.events.ProductUpdatedEvent
import ch.fitnesslab.product.infrastructure.ProductEntity
import ch.fitnesslab.product.infrastructure.ProductRepository
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@ProcessingGroup("products")
@Component
class ProductProjection(
    private val productRepository: ProductRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter,
) {
    @EventHandler
    fun on(event: ProductCreatedEvent) {
        val entity =
            ProductEntity(
                productId = event.productId.value,
                code = event.code,
                name = event.name,
                productType = event.productType,
                audience = event.audience,
                requiresMembership = event.requiresMembership,
                price = event.price,
                isTimeBased = event.behavior.isTimeBased,
                isSessionBased = event.behavior.isSessionBased,
                canBePaused = event.behavior.canBePaused,
                autoRenew = event.behavior.autoRenew,
                renewalLeadTimeDays = event.behavior.renewalLeadTimeDays,
                contributesToMembershipStatus = event.behavior.contributesToMembershipStatus,
                maxActivePerCustomer = event.behavior.maxActivePerCustomer,
                exclusivityGroup = event.behavior.exclusivityGroup,
            )
        productRepository.save(entity)

        queryUpdateEmitter.emit(
            FindAllProductsQuery::class.java,
            { true },
            ProductUpdatedUpdate(event.productId.value.toString()),
        )
    }

    @EventHandler
    fun on(event: ProductUpdatedEvent) {
        productRepository.findById(event.productId.value).ifPresent { existing ->
            val updated =
                ProductEntity(
                    productId = existing.productId,
                    code = event.code,
                    name = event.name,
                    productType = event.productType,
                    audience = event.audience,
                    requiresMembership = event.requiresMembership,
                    price = event.price,
                    isTimeBased = event.behavior.isTimeBased,
                    isSessionBased = event.behavior.isSessionBased,
                    canBePaused = event.behavior.canBePaused,
                    autoRenew = event.behavior.autoRenew,
                    renewalLeadTimeDays = event.behavior.renewalLeadTimeDays,
                    contributesToMembershipStatus = event.behavior.contributesToMembershipStatus,
                    maxActivePerCustomer = event.behavior.maxActivePerCustomer,
                    exclusivityGroup = event.behavior.exclusivityGroup,
                )
            productRepository.save(updated)

            queryUpdateEmitter.emit(
                FindAllProductsQuery::class.java,
                { true },
                ProductUpdatedUpdate(event.productId.value.toString()),
            )
            queryUpdateEmitter.emit(
                FindProductByIdQuery::class.java,
                { query -> query.productId == event.productId },
                ProductUpdatedUpdate(event.productId.value.toString()),
            )
        }
    }

    @QueryHandler
    fun handle(query: FindProductByIdQuery): ProductView? =
        productRepository
            .findById(query.productId.value)
            .map { toProductView(it) }
            .orElse(null)

    @QueryHandler
    fun handle(query: FindAllProductsQuery): List<ProductView> = productRepository.findAll().map { toProductView(it) }

    fun findById(productId: ProductVariantId): ProductView? =
        productRepository
            .findById(productId.value)
            .map { toProductView(it) }
            .orElse(null)

    fun findAll(): List<ProductView> = productRepository.findAll().map { toProductView(it) }

    private fun toProductView(productEntity: ProductEntity) =
        ProductView(
            productId = productEntity.productId.toString(),
            code = productEntity.code,
            name = productEntity.name,
            productType = productEntity.productType,
            audience = productEntity.audience.name,
            requiresMembership = productEntity.requiresMembership,
            price = productEntity.price,
            behavior =
                ch.fitnesslab.generated.model.ProductBehaviorConfig(
                    isTimeBased = productEntity.isTimeBased,
                    isSessionBased = productEntity.isSessionBased,
                    canBePaused = productEntity.canBePaused,
                    autoRenew = productEntity.autoRenew,
                    renewalLeadTimeDays = productEntity.renewalLeadTimeDays,
                    contributesToMembershipStatus = productEntity.contributesToMembershipStatus,
                    maxActivePerCustomer = productEntity.maxActivePerCustomer,
                    exclusivityGroup = productEntity.exclusivityGroup,
                ),
        )
}
