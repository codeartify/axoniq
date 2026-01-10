package ch.fitnesslab.booking.application

import ch.fitnesslab.domain.value.BookingId

data class FindAllBookingsQuery(
    val timestamp: Long = System.currentTimeMillis(),
)

data class FindBookingByIdQuery(
    val bookingId: BookingId,
)

data class BookingUpdatedUpdate(
    val bookingId: String,
)
