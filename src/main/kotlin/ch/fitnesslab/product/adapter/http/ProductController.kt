package ch.fitnesslab.product.adapter.http

import ch.fitnesslab.domain.value.ProductVariantId
import ch.fitnesslab.generated.api.ProductsApi
import ch.fitnesslab.generated.model.CreateProductRequest
import ch.fitnesslab.generated.model.ProductCreationResponse
import ch.fitnesslab.generated.model.ProductView
import ch.fitnesslab.generated.model.UpdateProductRequest
import ch.fitnesslab.product.application.FindAllProductsQuery
import ch.fitnesslab.product.application.ProductProjection
import ch.fitnesslab.product.application.ProductUpdatedUpdate
import ch.fitnesslab.product.domain.*
import ch.fitnesslab.product.domain.commands.CreateProductCommand
import ch.fitnesslab.product.domain.commands.UpdateProductCommand
import ch.fitnesslab.product.infrastructure.wix.WixSyncService
import ch.fitnesslab.utils.waitForUpdateOf
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
    private val productProjection: ProductProjection,
    private val wixSyncService: WixSyncService,
) : ProductsApi {
    override fun createProduct(createProductRequest: CreateProductRequest): ResponseEntity<ProductCreationResponse> {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllProductsQuery(),
                ResponseTypes.multipleInstancesOf(ProductView::class.java),
                ResponseTypes.instanceOf(ProductUpdatedUpdate::class.java),
            )

        try {
            val productId = ProductVariantId.generate()

            val command = toCreateProductCommand(productId, createProductRequest)
            commandGateway.sendAndWait<Any>(command)

            waitForUpdateOf(subscriptionQuery)

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductCreationResponse(productId.toString()))
        } finally {
            subscriptionQuery.close()
        }
    }

    override fun getProduct(productId: String): ResponseEntity<ProductView> {
        val product = productProjection.findById(ProductVariantId.from(productId))
        return if (product != null) {
            ResponseEntity.ok(product)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    override fun getAllProducts(): ResponseEntity<List<ProductView>> = ResponseEntity.ok(productProjection.findAll())

    override fun downloadFromWix(): ResponseEntity<Unit> {
        wixSyncService.syncWixProducts()
        return ResponseEntity.ok().build()
    }

    override fun uploadToWix(productId: String): ResponseEntity<Unit> {
        wixSyncService.uploadProduct(productId)
        return ResponseEntity.ok().build()
    }

    override fun updateProduct(
        productId: String,
        updateProductRequest: UpdateProductRequest,
    ): ResponseEntity<Unit> {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllProductsQuery(),
                ResponseTypes.multipleInstancesOf(ProductView::class.java),
                ResponseTypes.instanceOf(ProductUpdatedUpdate::class.java),
            )

        try {
            val command = toUpdateProductCommand(productId, updateProductRequest)

            commandGateway.sendAndWait<Any>(command)

            waitForUpdateOf(subscriptionQuery)

            wixSyncService.uploadProduct(productId)

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }

    private fun toCreateProductCommand(
        productId: ProductVariantId,
        createProductRequest: CreateProductRequest,
    ): CreateProductCommand =
        CreateProductCommand(
            productId = productId,
            slug = createProductRequest.slug,
            name = createProductRequest.name,
            productType = createProductRequest.productType,
            audience = createProductRequest.audience.let { ProductAudience.valueOf(it.name) },
            requiresMembership = createProductRequest.requiresMembership,
            pricingVariant =
                PricingVariantConfig(
                    pricingModel =
                        PricingModel.valueOf(
                            createProductRequest.pricingVariant.pricingModel.name,
                        ),
                    flatRate = createProductRequest.pricingVariant.flatRate,
                    billingCycle =
                        createProductRequest.pricingVariant.billingCycle?.let {
                            PricingDuration(
                                interval =
                                    BillingInterval.valueOf(
                                        it.interval.name,
                                    ),
                                count = it.count,
                            )
                        },
                    duration =
                        createProductRequest.pricingVariant.duration?.let {
                            PricingDuration(
                                interval =
                                    BillingInterval.valueOf(
                                        it.interval.name,
                                    ),
                                count = it.count,
                            )
                        },
                    freeTrial =
                        createProductRequest.pricingVariant.freeTrial?.let {
                            PricingDuration(
                                interval =
                                    BillingInterval.valueOf(
                                        it.interval.name,
                                    ),
                                count = it.count,
                            )
                        },
                ),
            behavior =
                ProductBehaviorConfig(
                    canBePaused = createProductRequest.behavior.canBePaused,
                    renewalLeadTimeDays = createProductRequest.behavior.renewalLeadTimeDays,
                    maxActivePerCustomer = createProductRequest.behavior.maxActivePerCustomer,
                    maxPurchasesPerBuyer = createProductRequest.behavior.maxPurchasesPerBuyer,
                    numberOfSessions = createProductRequest.behavior.numberOfSessions,
                ),
            description = createProductRequest.description,
            termsAndConditions = createProductRequest.termsAndConditions,
            visibility =
                createProductRequest.visibility?.let {
                    ProductVisibility
                        .valueOf(it.name)
                } ?: ProductVisibility.PUBLIC,
            buyable = createProductRequest.buyable ?: true,
            buyerCanCancel = createProductRequest.buyerCanCancel ?: true,
            perks = createProductRequest.perks,
        )

    private fun toUpdateProductCommand(
        productId: String,
        updateProductRequest: UpdateProductRequest,
    ): UpdateProductCommand =
        UpdateProductCommand(
            productId = ProductVariantId.from(productId),
            slug = updateProductRequest.slug,
            name = updateProductRequest.name,
            productType = updateProductRequest.productType,
            audience = updateProductRequest.audience.let { ProductAudience.valueOf(it.name) },
            requiresMembership = updateProductRequest.requiresMembership,
            pricingVariant =
                PricingVariantConfig(
                    pricingModel =
                        PricingModel.valueOf(
                            updateProductRequest.pricingVariant.pricingModel.name,
                        ),
                    flatRate = updateProductRequest.pricingVariant.flatRate,
                    billingCycle =
                        updateProductRequest.pricingVariant.billingCycle?.let {
                            PricingDuration(
                                interval =
                                    BillingInterval.valueOf(
                                        it.interval.name,
                                    ),
                                count = it.count,
                            )
                        },
                    duration =
                        updateProductRequest.pricingVariant.duration?.let {
                            PricingDuration(
                                interval =
                                    BillingInterval.valueOf(
                                        it.interval.name,
                                    ),
                                count = it.count,
                            )
                        },
                    freeTrial =
                        updateProductRequest.pricingVariant.freeTrial?.let {
                            PricingDuration(
                                interval =
                                    BillingInterval.valueOf(
                                        it.interval.name,
                                    ),
                                count = it.count,
                            )
                        },
                ),
            behavior =
                ProductBehaviorConfig(
                    canBePaused = updateProductRequest.behavior.canBePaused,
                    renewalLeadTimeDays = updateProductRequest.behavior.renewalLeadTimeDays,
                    maxActivePerCustomer = updateProductRequest.behavior.maxActivePerCustomer,
                    maxPurchasesPerBuyer = updateProductRequest.behavior.maxPurchasesPerBuyer,
                    numberOfSessions = updateProductRequest.behavior.numberOfSessions,
                ),
            description = updateProductRequest.description,
            termsAndConditions = updateProductRequest.termsAndConditions,
            visibility =
                updateProductRequest.visibility?.let {
                    ProductVisibility
                        .valueOf(it.name)
                } ?: ProductVisibility.PUBLIC,
            buyable = updateProductRequest.buyable ?: true,
            buyerCanCancel = updateProductRequest.buyerCanCancel ?: true,
            perks = updateProductRequest.perks,
            linkedPlatforms =
                updateProductRequest.linkedPlatforms?.map {
                    LinkedPlatformSync(
                        platformName = it.platformName,
                        idOnPlatform = it.idOnPlatform,
                        revision = it.revision,
                        visibilityOnPlatform =
                            it.visibilityOnPlatform?.let { visibility ->
                                PlatformVisibility.valueOf(visibility.name)
                            },
                        isSynced = it.isSynced,
                        isSourceOfTruth = it.isSourceOfTruth == true,
                        lastSyncedAt = it.lastSyncedAt?.toInstant(),
                        syncError = it.syncError,
                    )
                } ?: emptyList(),
        )
}
