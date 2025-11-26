package ch.fitnesslab.product.application

import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.product.domain.ProductAudience
import ch.fitnesslab.product.domain.ProductBehaviorConfig
import ch.fitnesslab.product.domain.events.ProductCreatedEvent
import ch.fitnesslab.product.domain.events.ProductUpdatedEvent
import ch.fitnesslab.product.infrastructure.ProductEntity
import ch.fitnesslab.product.infrastructure.ProductRepository
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ProductProjection(
    private val productRepository: ProductRepository
) {

    @EventHandler
    fun on(event: ProductCreatedEvent) {
        val entity = ProductEntity(
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
            exclusivityGroup = event.behavior.exclusivityGroup
        )
        productRepository.save(entity)
    }

    @EventHandler
    fun on(event: ProductUpdatedEvent) {
        productRepository.findById(event.productId.value).ifPresent { existing ->
            val updated = ProductEntity(
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
                exclusivityGroup = event.behavior.exclusivityGroup
            )
            productRepository.save(updated)
        }
    }

    fun findById(productId: ProductVariantId): ProductView? {
        return productRepository.findById(productId.value)
            .map { it.toProductView() }
            .orElse(null)
    }

    fun findAll(): List<ProductView> {
        return productRepository.findAll().map { it.toProductView() }
    }

    private fun ProductEntity.toProductView() = ProductView(
        productId = this.productId.toString(),
        code = this.code,
        name = this.name,
        productType = this.productType,
        audience = this.audience,
        requiresMembership = this.requiresMembership,
        price = this.price,
        behavior = ProductBehaviorConfig(
            isTimeBased = this.isTimeBased,
            isSessionBased = this.isSessionBased,
            canBePaused = this.canBePaused,
            autoRenew = this.autoRenew,
            renewalLeadTimeDays = this.renewalLeadTimeDays,
            contributesToMembershipStatus = this.contributesToMembershipStatus,
            maxActivePerCustomer = this.maxActivePerCustomer,
            exclusivityGroup = this.exclusivityGroup
        )
    )
}

data class ProductView(
    val productId: String,
    val code: String,
    val name: String,
    val productType: String,
    val audience: ProductAudience,
    val requiresMembership: Boolean,
    val price: BigDecimal,
    val behavior: ProductBehaviorConfig
)
