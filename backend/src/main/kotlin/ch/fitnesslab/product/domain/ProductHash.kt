package ch.fitnesslab.product.domain

import ch.fitnesslab.product.infrastructure.ProductVariantEntity
import java.security.MessageDigest

object ProductHash {
    fun computeHash(product: ProductVariantEntity): String {
        val data =
            buildString {
                append(product.slug)
                append(product.name)
                append(product.productType)
                append(product.audience.name)
                append(product.requiresMembership)
                append(product.pricingModel.name)
                append(product.flatRate)
                append(product.billingCycleInterval?.name ?: "")
                append(product.billingCycleCount ?: "")
                append(product.durationInterval?.name ?: "")
                append(product.durationCount ?: "")
                append(product.freeTrialInterval?.name ?: "")
                append(product.freeTrialCount ?: "")
                append(product.canBePaused)
                append(product.renewalLeadTimeDays ?: "")
                append(product.maxActivePerCustomer ?: "")
                append(product.maxPurchasesPerBuyer ?: "")
                append(product.numberOfSessions ?: "")
                append(product.description ?: "")
                append(product.termsAndConditions ?: "")
                append(product.visibility.name)
                append(product.buyable)
                append(product.buyerCanCancel)
                append(product.perks?.joinToString(",") ?: "")
            }
        return MessageDigest
            .getInstance("SHA-256")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
