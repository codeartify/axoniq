package ch.fitnesslab.product.domain

import ch.fitnesslab.domain.value.ProductId
import ch.fitnesslab.product.domain.commands.AddLinkedPlatformCommand
import ch.fitnesslab.product.domain.commands.CreateProductCommand
import ch.fitnesslab.product.domain.commands.UpdateProductCommand
import ch.fitnesslab.product.domain.events.LinkedPlatformAddedEvent
import ch.fitnesslab.product.domain.events.ProductCreatedEvent
import ch.fitnesslab.product.domain.events.ProductUpdatedEvent
import org.axonframework.eventsourcing.annotation.EventSourcingHandler
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator
import org.axonframework.extension.spring.stereotype.EventSourced
import org.axonframework.messaging.commandhandling.annotation.CommandHandler
import org.axonframework.messaging.eventhandling.gateway.EventAppender

@EventSourced(idType = ProductId::class, tagKey = "Product")
class Product {
    private lateinit var productId: ProductId
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
    private var linkedPlatforms: List<LinkedPlatformSync>? = null

    @EntityCreator
    constructor()

    companion object {
        @JvmStatic
        @CommandHandler
        fun handle(
            command: CreateProductCommand,
            eventAppender: EventAppender,
        ) {
            eventAppender.append(
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
                    linkedPlatforms = command.linkedPlatforms,
                ),
            )
        }

        @JvmStatic
        @CommandHandler
        fun createFromUpdate(
            command: UpdateProductCommand,
            eventAppender: EventAppender,
        ) {
            appendProductUpdatedEvent(command, eventAppender)
        }

        private fun appendProductUpdatedEvent(
            command: UpdateProductCommand,
            eventAppender: EventAppender,
        ) {
            eventAppender.append(
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
                    linkedPlatforms = command.linkedPlatforms,
                ),
            )
        }
    }

    @CommandHandler
    fun handle(
        command: UpdateProductCommand,
        eventAppender: EventAppender,
    ) {
        appendProductUpdatedEvent(command, eventAppender)
    }

    @CommandHandler
    fun on(
        event: AddLinkedPlatformCommand,
        eventAppender: EventAppender,
    ) {
        eventAppender.append(
            LinkedPlatformAddedEvent(
                productId = event.productId,
                linkedPlatforms = event.linkedPlatforms,
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
        this.linkedPlatforms = event.linkedPlatforms
    }

    @EventSourcingHandler
    fun on(event: ProductUpdatedEvent) {
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
        this.linkedPlatforms = event.linkedPlatforms
    }

    @EventSourcingHandler
    fun on(event: LinkedPlatformAddedEvent) {
        this.linkedPlatforms = event.linkedPlatforms
    }
}
