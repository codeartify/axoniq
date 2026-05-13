package ch.fitnesslab.booking.domain

import ch.fitnesslab.booking.domain.commands.PlaceBookingCommand
import ch.fitnesslab.booking.domain.events.BookingPlacedEvent
import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.eventsourcing.annotation.EventSourcingHandler
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator
import org.axonframework.extension.spring.stereotype.EventSourced
import org.axonframework.messaging.commandhandling.annotation.CommandHandler
import org.axonframework.messaging.eventhandling.gateway.EventAppender

@EventSourced(idType = BookingId::class, tagKey = "Booking")
class Booking {
    private lateinit var bookingId: BookingId
    private lateinit var payerCustomerId: CustomerId
    private lateinit var status: BookingStatus
    private lateinit var purchasedProducts: List<PurchasedProduct>
    private val invoiceIds: MutableList<InvoiceId> = mutableListOf()

    @EntityCreator
    constructor()

    companion object {
        @JvmStatic
        @CommandHandler
        fun handle(
            command: PlaceBookingCommand,
            eventAppender: EventAppender,
        ) {
            require(command.purchasedProducts.isNotEmpty()) { "Booking must contain at least one product" }

            eventAppender.append(
                BookingPlacedEvent(
                    bookingId = command.bookingId,
                    payerCustomerId = command.payerCustomerId,
                    purchasedProducts = command.purchasedProducts,
                    status = BookingStatus.PENDING,
                ),
            )
        }
    }

    @EventSourcingHandler
    fun on(event: BookingPlacedEvent) {
        this.bookingId = event.bookingId
        this.payerCustomerId = event.payerCustomerId
        this.purchasedProducts = event.purchasedProducts
        this.status = event.status
    }
}
