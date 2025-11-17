package ch.fitnesslab.booking.domain.events

import ch.fitnesslab.booking.domain.BookingStatus
import ch.fitnesslab.booking.domain.PurchasedProduct
import ch.fitnesslab.common.types.BookingId
import ch.fitnesslab.common.types.CustomerId

data class BookingPlacedEvent(
    val bookingId: BookingId,
    val payerCustomerId: CustomerId,
    val purchasedProducts: List<PurchasedProduct>,
    val status: BookingStatus
)
