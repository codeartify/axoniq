package ch.fitnesslab.product.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductRepository : JpaRepository<ProductVariantEntity, UUID> {
    fun findProductByWixId(wixId: String): ProductVariantEntity? = findAll()
        .firstOrNull { product -> isWixProductWithWixId(product, wixId) }

    private fun isWixProductWithWixId(
        product: ProductVariantEntity?,
        id: String
    ): Boolean = product?.linkedPlatforms?.any {
        it.platformName == "wix" && it.idOnPlatform == id
    } == true

}
