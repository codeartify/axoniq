package ch.fitnesslab.booking.domain

import ch.fitnesslab.booking.domain.commands.PlaceBookingCommand
import ch.fitnesslab.booking.domain.events.BookingPlacedEvent
import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class Booking() {
    @AggregateIdentifier
    private lateinit var bookingId: BookingId
    private lateinit var payerCustomerId: CustomerId
    private lateinit var status: BookingStatus
    private lateinit var purchasedProducts: List<PurchasedProduct>
    private val invoiceIds: MutableList<InvoiceId> = mutableListOf()

    @CommandHandler
    constructor(command: PlaceBookingCommand) : this() {
        require(command.purchasedProducts.isNotEmpty()) { "Booking must contain at least one product" }

        AggregateLifecycle.apply(
            BookingPlacedEvent(
                bookingId = command.bookingId,
                payerCustomerId = command.payerCustomerId,
                purchasedProducts = command.purchasedProducts,
                status = BookingStatus.PENDING,
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: BookingPlacedEvent) {
        this.bookingId = event.bookingId
        this.payerCustomerId = event.payerCustomerId
        this.purchasedProducts = event.purchasedProducts
        this.status = event.status
    }
}
