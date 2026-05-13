package ch.fitnesslab.product.domain.commands

import ch.fitnesslab.domain.value.ProductId
import ch.fitnesslab.product.domain.LinkedPlatformSync
import org.axonframework.modelling.annotation.TargetEntityId

data class AddLinkedPlatformCommand(
    @TargetEntityId
    val productId: ProductId,
    val linkedPlatforms: List<LinkedPlatformSync>? = null,
)
