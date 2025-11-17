package ch.fitnesslab.product.adapter.http

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/api/products")
class ProductController {

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

data class ProductVariantDto(
    val id: String,
    val code: String,
    val name: String,
    val productType: String,
    val price: BigDecimal,
    val durationMonths: Int
)
