package ch.fitnesslab.product.adapter.http

import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.product.application.ProductProjection
import ch.fitnesslab.product.application.ProductView
import ch.fitnesslab.product.domain.ProductAudience
import ch.fitnesslab.product.domain.ProductBehaviorConfig
import ch.fitnesslab.product.domain.commands.CreateProductCommand
import ch.fitnesslab.product.domain.commands.UpdateProductCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val commandGateway: CommandGateway,
    private val productProjection: ProductProjection
) {

    @PostMapping
    fun createProduct(@RequestBody request: CreateProductRequest): ResponseEntity<ProductCreationResponse> {
        val productId = ProductVariantId.generate()

        val command = CreateProductCommand(
            productId = productId,
            code = request.code,
            name = request.name,
            productType = request.productType,
            audience = request.audience,
            requiresMembership = request.requiresMembership,
            price = request.price,
            behavior = request.behavior
        )
        commandGateway.sendAndWait<Any>(command)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ProductCreationResponse(productId.toString()))
    }

    @GetMapping("/{productId}")
    fun getProduct(@PathVariable productId: String): ResponseEntity<ProductView> {
        val product = productProjection.findById(ProductVariantId.from(productId))
        return if (product != null) {
            ResponseEntity.ok(product)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    fun getAllProducts(): ResponseEntity<List<ProductView>> {
        return ResponseEntity.ok(productProjection.findAll())
    }

    @PutMapping("/{productId}")
    fun updateProduct(
        @PathVariable productId: String,
        @RequestBody request: UpdateProductRequest
    ): ResponseEntity<Void> {
        val command = UpdateProductCommand(
            productId = ProductVariantId.from(productId),
            code = request.code,
            name = request.name,
            productType = request.productType,
            audience = request.audience,
            requiresMembership = request.requiresMembership,
            price = request.price,
            behavior = request.behavior
        )

        commandGateway.sendAndWait<Any>(command)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/memberships")
    fun getMembershipVariants(): ResponseEntity<List<ProductVariantDto>> {
        // TODO: Replace with actual repository/service call
        val variants = listOf(
            ProductVariantDto(
                id = "550e8400-e29b-41d4-a716-446655440001",
                code = "FITNESS_12M",
                name = "Fitness Membership 12 Months",
                productType = "MEMBERSHIP",
                price = BigDecimal("1200.00"),
                durationMonths = 12
            ),
            ProductVariantDto(
                id = "550e8400-e29b-41d4-a716-446655440002",
                code = "FITNESS_6M",
                name = "Fitness Membership 6 Months",
                productType = "MEMBERSHIP",
                price = BigDecimal("660.00"),
                durationMonths = 6
            )
        )
        return ResponseEntity.ok(variants)
    }
}

data class CreateProductRequest(
    val code: String,
    val name: String,
    val productType: String,
    val audience: ProductAudience,
    val requiresMembership: Boolean,
    val price: BigDecimal,
    val behavior: ProductBehaviorConfig
)

data class UpdateProductRequest(
    val code: String,
    val name: String,
    val productType: String,
    val audience: ProductAudience,
    val requiresMembership: Boolean,
    val price: BigDecimal,
    val behavior: ProductBehaviorConfig
)

data class ProductCreationResponse(
    val productId: String? = null,
    val error: String? = null
)

data class ProductVariantDto(
    val id: String,
    val code: String,
    val name: String,
    val productType: String,
    val price: BigDecimal,
    val durationMonths: Int
)
