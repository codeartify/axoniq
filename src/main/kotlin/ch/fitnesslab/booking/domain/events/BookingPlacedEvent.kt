package ch.fitnesslab.booking.domain.events

import ch.fitnesslab.booking.domain.BookingStatus
import ch.fitnesslab.booking.domain.PurchasedProduct
import ch.fitnesslab.common.types.BookingId
import ch.fitnesslab.common.types.CustomerId
import org.axonframework.serialization.Revision

@Revision("1.0")
data class BookingPlacedEvent(
    val bookingId: BookingId,
    val payerCustomerId: CustomerId,
    val purchasedProducts: List<PurchasedProduct>,
    val status: BookingStatus
)
