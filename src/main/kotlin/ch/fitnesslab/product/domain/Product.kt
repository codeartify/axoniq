package ch.fitnesslab.product.domain

import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.product.domain.commands.CreateProductCommand
import ch.fitnesslab.product.domain.commands.UpdateProductCommand
import ch.fitnesslab.product.domain.events.ProductCreatedEvent
import ch.fitnesslab.product.domain.events.ProductUpdatedEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class Product() {
    @AggregateIdentifier
    private lateinit var productId: ProductVariantId
    private lateinit var slug: String
    private lateinit var name: String
    private lateinit var productType: String
    private lateinit var audience: ProductAudience
    private var requiresMembership: Boolean = false
    private lateinit var pricingVariant: PricingVariantConfig
    private lateinit var behavior: ProductBehaviorConfig
    private var description: String? = null
    private var termsAndConditions: String? = null
    private lateinit var visibility: ProductVisibility
    private var buyable: Boolean = true
    private var buyerCanCancel: Boolean = true
    private var perks: List<String>? = null

    @CommandHandler
    constructor(command: CreateProductCommand) : this() {
        AggregateLifecycle.apply(
            ProductCreatedEvent(
                productId = command.productId,
                slug = command.slug,
                name = command.name,
                productType = command.productType,
                audience = command.audience,
                requiresMembership = command.requiresMembership,
                pricingVariant = command.pricingVariant,
                behavior = command.behavior,
                description = command.description,
                termsAndConditions = command.termsAndConditions,
                visibility = command.visibility,
                buyable = command.buyable,
                buyerCanCancel = command.buyerCanCancel,
                perks = command.perks,
            ),
        )
    }

    @CommandHandler
    fun handle(command: UpdateProductCommand) {
        AggregateLifecycle.apply(
            ProductUpdatedEvent(
                productId = command.productId,
                slug = command.slug,
                name = command.name,
                productType = command.productType,
                audience = command.audience,
                requiresMembership = command.requiresMembership,
                pricingVariant = command.pricingVariant,
                behavior = command.behavior,
                description = command.description,
                termsAndConditions = command.termsAndConditions,
                visibility = command.visibility,
                buyable = command.buyable,
                buyerCanCancel = command.buyerCanCancel,
                perks = command.perks,
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: ProductCreatedEvent) {
        this.productId = event.productId
        this.slug = event.slug
        this.name = event.name
        this.productType = event.productType
        this.audience = event.audience
        this.requiresMembership = event.requiresMembership
        this.pricingVariant = event.pricingVariant
        this.behavior = event.behavior
        this.description = event.description
        this.termsAndConditions = event.termsAndConditions
        this.visibility = event.visibility
        this.buyable = event.buyable
        this.buyerCanCancel = event.buyerCanCancel
        this.perks = event.perks
    }

    @EventSourcingHandler
    fun on(event: ProductUpdatedEvent) {
        this.slug = event.slug
        this.name = event.name
        this.productType = event.productType
        this.audience = event.audience
        this.requiresMembership = event.requiresMembership
        this.pricingVariant = event.pricingVariant
        this.behavior = event.behavior
        this.description = event.description
        this.termsAndConditions = event.termsAndConditions
        this.visibility = event.visibility
        this.buyable = event.buyable
        this.buyerCanCancel = event.buyerCanCancel
        this.perks = event.perks
    }
}
