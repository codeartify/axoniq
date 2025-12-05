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
                    code = request.code,
                    name = request.name,
                    productType = request.productType,
                    audience = request.audience.let { ProductAudience.valueOf(it.name) },
                    requiresMembership = request.requiresMembership,
                    price = request.price,
                    behavior =
                        ProductBehaviorConfig(
                            canBePaused = request.behavior.canBePaused,
                            renewalLeadTimeDays = request.behavior.renewalLeadTimeDays,
                            maxActivePerCustomer = request.behavior.maxActivePerCustomer,
                            durationInMonths = request.behavior.durationInMonths,
                            numberOfSessions = request.behavior.numberOfSessions,
                        ),
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
    fun getAllProducts(): ResponseEntity<List<ProductView>> = ResponseEntity.ok(productProjection.findAll())

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
                    code = request.code,
                    name = request.name,
                    productType = request.productType,
                    audience = request.audience.let { ProductAudience.valueOf(it.name) },
                    requiresMembership = request.requiresMembership,
                    price = request.price,
                    behavior =
                        ProductBehaviorConfig(
                            canBePaused = request.behavior.canBePaused,
                            renewalLeadTimeDays = request.behavior.renewalLeadTimeDays,
                            maxActivePerCustomer = request.behavior.maxActivePerCustomer,
                            durationInMonths = request.behavior.durationInMonths,
                            numberOfSessions = request.behavior.numberOfSessions,
                        ),
                )

            commandGateway.sendAndWait<Any>(command)

            waitForUpdateOf(subscriptionQuery)

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }
}
