package ch.fitnesslab.booking.domain.events

import ch.fitnesslab.booking.domain.BookingStatus
import ch.fitnesslab.booking.domain.PurchasedProduct
import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.CustomerId
import org.axonframework.eventsourcing.annotation.EventTag

data class BookingPlacedEvent(
    @field:EventTag(key = "Booking")
    val bookingId: BookingId,
    val payerCustomerId: CustomerId,
    val purchasedProducts: List<PurchasedProduct>,
    val status: BookingStatus,
)
