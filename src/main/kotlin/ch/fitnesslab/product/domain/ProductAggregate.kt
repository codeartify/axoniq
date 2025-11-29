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
import java.math.BigDecimal

@Aggregate
class ProductAggregate() {
    @AggregateIdentifier
    private lateinit var productId: ProductVariantId
    private lateinit var code: String
    private lateinit var name: String
    private lateinit var productType: String
    private lateinit var audience: ProductAudience
    private var requiresMembership: Boolean = false
    private lateinit var price: BigDecimal
    private lateinit var behavior: ProductBehaviorConfig

    @CommandHandler
    constructor(command: CreateProductCommand) : this() {
        AggregateLifecycle.apply(
            ProductCreatedEvent(
                productId = command.productId,
                code = command.code,
                name = command.name,
                productType = command.productType,
                audience = command.audience,
                requiresMembership = command.requiresMembership,
                price = command.price,
                behavior = command.behavior,
            ),
        )
    }

    @CommandHandler
    fun handle(command: UpdateProductCommand) {
        AggregateLifecycle.apply(
            ProductUpdatedEvent(
                productId = command.productId,
                code = command.code,
                name = command.name,
                productType = command.productType,
                audience = command.audience,
                requiresMembership = command.requiresMembership,
                price = command.price,
                behavior = command.behavior,
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: ProductCreatedEvent) {
        this.productId = event.productId
        this.code = event.code
        this.name = event.name
        this.productType = event.productType
        this.audience = event.audience
        this.requiresMembership = event.requiresMembership
        this.price = event.price
        this.behavior = event.behavior
    }

    @EventSourcingHandler
    fun on(event: ProductUpdatedEvent) {
        this.code = event.code
        this.name = event.name
        this.productType = event.productType
        this.audience = event.audience
        this.requiresMembership = event.requiresMembership
        this.price = event.price
        this.behavior = event.behavior
    }
}
