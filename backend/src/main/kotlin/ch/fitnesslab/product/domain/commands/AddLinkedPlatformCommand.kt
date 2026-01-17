package ch.fitnesslab.product.domain.commands

import ch.fitnesslab.domain.value.ProductId
import ch.fitnesslab.product.domain.LinkedPlatformSync
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class AddLinkedPlatformCommand(
    @TargetAggregateIdentifier
    val productId: ProductId,
    val linkedPlatforms: List<LinkedPlatformSync>? = null,
)
