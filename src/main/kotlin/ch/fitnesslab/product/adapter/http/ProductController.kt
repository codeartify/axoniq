package ch.fitnesslab.product.adapter.http

import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.generated.model.CreateProductRequest
import ch.fitnesslab.generated.model.ProductCreationResponse
import ch.fitnesslab.generated.model.ProductView
import ch.fitnesslab.generated.model.UpdateProductRequest
import ch.fitnesslab.product.application.FindAllProductsQuery
import ch.fitnesslab.product.application.ProductProjection
import ch.fitnesslab.product.application.ProductUpdatedUpdate
import ch.fitnesslab.product.domain.ProductAudience
import ch.fitnesslab.product.domain.ProductBehaviorConfig
import ch.fitnesslab.product.domain.commands.CreateProductCommand
import ch.fitnesslab.product.domain.commands.UpdateProductCommand
import ch.fitnesslab.product.infrastructure.wix.WixSyncService
import ch.fitnesslab.utils.waitForUpdateOf
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
    private val productProjection: ProductProjection,
    private val wixSyncService: WixSyncService,
) {
    @PostMapping
    fun createProduct(
        @RequestBody request: CreateProductRequest,
    ): ResponseEntity<ProductCreationResponse> {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllProductsQuery(),
                ResponseTypes.multipleInstancesOf(ProductView::class.java),
                ResponseTypes.instanceOf(ProductUpdatedUpdate::class.java),
            )

        try {
            val productId = ProductVariantId.generate()

            val command =
                CreateProductCommand(
                    productId = productId,
                    slug = request.slug,
                    name = request.name,
                    productType = request.productType,
                    audience = request.audience.let { ProductAudience.valueOf(it.name) },
                    requiresMembership = request.requiresMembership,
                    pricingVariant =
                        ch.fitnesslab.product.domain.PricingVariantConfig(
                            pricingModel =
                                ch.fitnesslab.product.domain.PricingModel.valueOf(
                                    request.pricingVariant.pricingModel.name,
                                ),
                            flatRate = request.pricingVariant.flatRate,
                            billingCycle =
                                request.pricingVariant.billingCycle?.let {
                                    ch.fitnesslab.product.domain.PricingDuration(
                                        interval =
                                            ch.fitnesslab.product.domain.BillingInterval.valueOf(
                                                it.interval.name,
                                            ),
                                        count = it.count,
                                    )
                                },
                            duration =
                                request.pricingVariant.duration?.let {
                                    ch.fitnesslab.product.domain.PricingDuration(
                                        interval =
                                            ch.fitnesslab.product.domain.BillingInterval.valueOf(
                                                it.interval.name,
                                            ),
                                        count = it.count,
                                    )
                                },
                            freeTrial =
                                request.pricingVariant.freeTrial?.let {
                                    ch.fitnesslab.product.domain.PricingDuration(
                                        interval =
                                            ch.fitnesslab.product.domain.BillingInterval.valueOf(
                                                it.interval.name,
                                            ),
                                        count = it.count,
                                    )
                                },
                        ),
                    behavior =
                        ProductBehaviorConfig(
                            canBePaused = request.behavior.canBePaused,
                            renewalLeadTimeDays = request.behavior.renewalLeadTimeDays,
                            maxActivePerCustomer = request.behavior.maxActivePerCustomer,
                            maxPurchasesPerBuyer = request.behavior.maxPurchasesPerBuyer,
                            numberOfSessions = request.behavior.numberOfSessions,
                        ),
                    description = request.description,
                    termsAndConditions = request.termsAndConditions,
                    visibility =
                        request.visibility?.let {
                            ch.fitnesslab.product.domain.ProductVisibility.valueOf(it.name)
                        } ?: ch.fitnesslab.product.domain.ProductVisibility.PUBLIC,
                    buyable = request.buyable ?: true,
                    buyerCanCancel = request.buyerCanCancel ?: true,
                    perks = request.perks,
                )
            commandGateway.sendAndWait<Any>(command)

            waitForUpdateOf(subscriptionQuery)

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductCreationResponse(productId.toString()))
        } finally {
            subscriptionQuery.close()
        }
    }

    @GetMapping("/{productId}")
    fun getProduct(
        @PathVariable productId: String,
    ): ResponseEntity<ProductView> {
        val product = productProjection.findById(ProductVariantId.from(productId))
        return if (product != null) {
            ResponseEntity.ok(product)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    fun getAllProducts(): ResponseEntity<List<ProductView>> {
        // Sync with Wix before fetching products
        wixSyncService.syncWixProducts()
        return ResponseEntity.ok(productProjection.findAll())
    }

    @PutMapping("/{productId}")
    fun updateProduct(
        @PathVariable productId: String,
        @RequestBody request: UpdateProductRequest,
    ): ResponseEntity<Void> {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllProductsQuery(),
                ResponseTypes.multipleInstancesOf(ProductView::class.java),
                ResponseTypes.instanceOf(ProductUpdatedUpdate::class.java),
            )

        try {
            val command =
                UpdateProductCommand(
                    productId = ProductVariantId.from(productId),
                    slug = request.slug,
                    name = request.name,
                    productType = request.productType,
                    audience = request.audience.let { ProductAudience.valueOf(it.name) },
                    requiresMembership = request.requiresMembership,
                    pricingVariant =
                        ch.fitnesslab.product.domain.PricingVariantConfig(
                            pricingModel =
                                ch.fitnesslab.product.domain.PricingModel.valueOf(
                                    request.pricingVariant.pricingModel.name,
                                ),
                            flatRate = request.pricingVariant.flatRate,
                            billingCycle =
                                request.pricingVariant.billingCycle?.let {
                                    ch.fitnesslab.product.domain.PricingDuration(
                                        interval =
                                            ch.fitnesslab.product.domain.BillingInterval.valueOf(
                                                it.interval.name,
                                            ),
                                        count = it.count,
                                    )
                                },
                            duration =
                                request.pricingVariant.duration?.let {
                                    ch.fitnesslab.product.domain.PricingDuration(
                                        interval =
                                            ch.fitnesslab.product.domain.BillingInterval.valueOf(
                                                it.interval.name,
                                            ),
                                        count = it.count,
                                    )
                                },
                            freeTrial =
                                request.pricingVariant.freeTrial?.let {
                                    ch.fitnesslab.product.domain.PricingDuration(
                                        interval =
                                            ch.fitnesslab.product.domain.BillingInterval.valueOf(
                                                it.interval.name,
                                            ),
                                        count = it.count,
                                    )
                                },
                        ),
                    behavior =
                        ProductBehaviorConfig(
                            canBePaused = request.behavior.canBePaused,
                            renewalLeadTimeDays = request.behavior.renewalLeadTimeDays,
                            maxActivePerCustomer = request.behavior.maxActivePerCustomer,
                            maxPurchasesPerBuyer = request.behavior.maxPurchasesPerBuyer,
                            numberOfSessions = request.behavior.numberOfSessions,
                        ),
                    description = request.description,
                    termsAndConditions = request.termsAndConditions,
                    visibility =
                        request.visibility?.let {
                            ch.fitnesslab.product.domain.ProductVisibility.valueOf(it.name)
                        } ?: ch.fitnesslab.product.domain.ProductVisibility.PUBLIC,
                    buyable = request.buyable ?: true,
                    buyerCanCancel = request.buyerCanCancel ?: true,
                    perks = request.perks,
                )

            commandGateway.sendAndWait<Any>(command)

            waitForUpdateOf(subscriptionQuery)

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }
}
