package ch.fitnesslab.product.application

import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.generated.model.*
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
                slug = event.slug,
                name = event.name,
                productType = event.productType,
                audience = event.audience,
                requiresMembership = event.requiresMembership,

                // PricingVariantConfig fields (flattened)
                pricingModel = event.pricingVariant.pricingModel,
                flatRate = event.pricingVariant.flatRate,
                billingCycleInterval = event.pricingVariant.billingCycle?.interval,
                billingCycleCount = event.pricingVariant.billingCycle?.count,
                durationInterval = event.pricingVariant.duration?.interval,
                durationCount = event.pricingVariant.duration?.count,
                freeTrialInterval = event.pricingVariant.freeTrial?.interval,
                freeTrialCount = event.pricingVariant.freeTrial?.count,

                // ProductBehaviorConfig fields (flattened)
                canBePaused = event.behavior.canBePaused,
                renewalLeadTimeDays = event.behavior.renewalLeadTimeDays,
                maxActivePerCustomer = event.behavior.maxActivePerCustomer,
                maxPurchasesPerBuyer = event.behavior.maxPurchasesPerBuyer,
                numberOfSessions = event.behavior.numberOfSessions,

                // New fields
                description = event.description,
                termsAndConditions = event.termsAndConditions,
                visibility = event.visibility,
                buyable = event.buyable,
                buyerCanCancel = event.buyerCanCancel,
                perks = event.perks,
                linkedPlatforms = event.linkedPlatforms,
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
                    slug = event.slug,
                    name = event.name,
                    productType = event.productType,
                    audience = event.audience,
                    requiresMembership = event.requiresMembership,

                    // PricingVariantConfig fields (flattened)
                    pricingModel = event.pricingVariant.pricingModel,
                    flatRate = event.pricingVariant.flatRate,
                    billingCycleInterval = event.pricingVariant.billingCycle?.interval,
                    billingCycleCount = event.pricingVariant.billingCycle?.count,
                    durationInterval = event.pricingVariant.duration?.interval,
                    durationCount = event.pricingVariant.duration?.count,
                    freeTrialInterval = event.pricingVariant.freeTrial?.interval,
                    freeTrialCount = event.pricingVariant.freeTrial?.count,

                    // ProductBehaviorConfig fields (flattened)
                    canBePaused = event.behavior.canBePaused,
                    renewalLeadTimeDays = event.behavior.renewalLeadTimeDays,
                    maxActivePerCustomer = event.behavior.maxActivePerCustomer,
                    maxPurchasesPerBuyer = event.behavior.maxPurchasesPerBuyer,
                    numberOfSessions = event.behavior.numberOfSessions,

                    // New fields
                    description = event.description,
                    termsAndConditions = event.termsAndConditions,
                    visibility = event.visibility,
                    buyable = event.buyable,
                    buyerCanCancel = event.buyerCanCancel,
                    perks = event.perks,
                    linkedPlatforms = event.linkedPlatforms,
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
    fun handle(query: FindProductByIdQuery): ProductVariantEntity? =
        productRepository
            .findById(query.productId.value)
            .orElse(null)

    @QueryHandler
    fun handle(query: FindAllProductsQuery): List<ProductView> = productRepository.findAll().map { toProductView(it) }

    fun findById(productId: ProductVariantId): ProductView? =
        productRepository
            .findById(productId.value)
            .map { toProductView(it) }
            .orElse(null)

    fun findAll(): List<ProductView> = productRepository.findAll().map { toProductView(it) }

    private fun toProductView(productVariantEntity: ProductVariantEntity): ProductView {
        val pricingVariant =
            PricingVariantConfig(
                pricingModel =
                    PricingModel.valueOf(
                        productVariantEntity.pricingModel.name,
                    ),
                flatRate = productVariantEntity.flatRate,
                billingCycle =
                    if (productVariantEntity.billingCycleInterval != null &&
                        productVariantEntity.billingCycleCount != null
                    ) {
                        // TODO Fix
                        PricingDuration(
                            interval =
                                productVariantEntity.billingCycleInterval?.let { BillingInterval.valueOf(it.name) }
                                    ?: BillingInterval.MONTH,
                            count = productVariantEntity.billingCycleCount ?: 0,
                        )
                    } else {
                        null
                    },
                duration =
                    if (productVariantEntity.durationInterval != null &&
                        productVariantEntity.durationCount != null
                    ) {
                        //TODO fix
                        PricingDuration(
                            interval =
                                productVariantEntity.durationInterval?.let { BillingInterval.valueOf(it.name) }
                                    ?: BillingInterval.MONTH,
                            count = productVariantEntity.durationCount ?: 0,
                        )
                    } else {
                        null
                    },
                freeTrial =
                    if (productVariantEntity.freeTrialInterval != null &&
                        productVariantEntity.freeTrialCount != null
                    ) {
                        PricingDuration(
                            interval =
                                productVariantEntity.freeTrialInterval?.let { BillingInterval.valueOf(it.name) }
                                    ?: BillingInterval.MONTH,
                            count = productVariantEntity.freeTrialCount ?: 0,
                        )
                    } else {
                        null
                    },
            )

        val behavior =
            ProductBehaviorConfig(
                canBePaused = productVariantEntity.canBePaused,
                renewalLeadTimeDays = productVariantEntity.renewalLeadTimeDays,
                maxActivePerCustomer = productVariantEntity.maxActivePerCustomer,
                maxPurchasesPerBuyer = productVariantEntity.maxPurchasesPerBuyer,
                numberOfSessions = productVariantEntity.numberOfSessions,
            )

        return ProductView(
            productId = productVariantEntity.productId.toString(),
            slug = productVariantEntity.slug,
            name = productVariantEntity.name,
            productType = productVariantEntity.productType,
            audience = productVariantEntity.audience.name,
            requiresMembership = productVariantEntity.requiresMembership,
            pricingVariant = pricingVariant,
            behavior = behavior,
            description = productVariantEntity.description,
            termsAndConditions = productVariantEntity.termsAndConditions,
            visibility =
                ProductView.Visibility.valueOf(
                    productVariantEntity.visibility.name,
                ),
            buyable = productVariantEntity.buyable,
            buyerCanCancel = productVariantEntity.buyerCanCancel,
            perks = productVariantEntity.perks,
            linkedPlatforms = productVariantEntity.linkedPlatforms?.map { toLinkedPlatformSyncView(it) },
        )
    }

    private fun toLinkedPlatformSyncView(
        domain: ch.fitnesslab.product.domain.LinkedPlatformSync
    ): ch.fitnesslab.generated.model.LinkedPlatformSync {
        return ch.fitnesslab.generated.model.LinkedPlatformSync(
            platformName = domain.platformName,
            idOnPlatform = domain.idOnPlatform,
            revision = domain.revision,
            visibilityOnPlatform = domain.visibilityOnPlatform?.let {
                ch.fitnesslab.generated.model.LinkedPlatformSync.VisibilityOnPlatform.valueOf(it.name)
            },
            isSynced = domain.isSynced,
            isSourceOfTruth = domain.isSourceOfTruth,
            lastSyncedAt = domain.lastSyncedAt?.atOffset(java.time.ZoneOffset.UTC),
            syncError = domain.syncError
        )
    }
}
