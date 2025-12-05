package ch.fitnesslab.product.application

import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.generated.model.ProductView
import ch.fitnesslab.product.domain.events.ProductCreatedEvent
import ch.fitnesslab.product.domain.events.ProductUpdatedEvent
import ch.fitnesslab.product.infrastructure.ProductRepository
import ch.fitnesslab.product.infrastructure.ProductVariantEntity
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
            ProductVariantEntity(
                productId = event.productId.value,
                code = event.code,
                name = event.name,
                productType = event.productType,
                audience = event.audience,
                requiresMembership = event.requiresMembership,
                price = event.price,
                canBePaused = event.behavior.canBePaused,
                durationInMonths = event.behavior.durationInMonths,
                renewalLeadTimeDays = event.behavior.renewalLeadTimeDays,
                maxActivePerCustomer = event.behavior.maxActivePerCustomer,
                numberOfSessions = event.behavior.numberOfSessions,
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
                ProductVariantEntity(
                    productId = existing.productId,
                    code = event.code,
                    name = event.name,
                    productType = event.productType,
                    audience = event.audience,
                    requiresMembership = event.requiresMembership,
                    price = event.price,
                    canBePaused = event.behavior.canBePaused,
                    renewalLeadTimeDays = event.behavior.renewalLeadTimeDays,
                    maxActivePerCustomer = event.behavior.maxActivePerCustomer,
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

    private fun toProductView(productVariantEntity: ProductVariantEntity) =
        ProductView(
            productId = productVariantEntity.productId.toString(),
            code = productVariantEntity.code,
            name = productVariantEntity.name,
            productType = productVariantEntity.productType,
            audience = productVariantEntity.audience.name,
            requiresMembership = productVariantEntity.requiresMembership,
            price = productVariantEntity.price,
            behavior =
                ch.fitnesslab.generated.model.ProductBehaviorConfig(
                    canBePaused = productVariantEntity.canBePaused,
                    renewalLeadTimeDays = productVariantEntity.renewalLeadTimeDays,
                    maxActivePerCustomer = productVariantEntity.maxActivePerCustomer,
                    durationInMonths = productVariantEntity.durationInMonths,
                    numberOfSessions = productVariantEntity.numberOfSessions,
                ),
        )
}
