package ch.fitnesslab.booking.application

import ch.fitnesslab.common.types.BookingId

data class FindAllBookingsQuery(val timestamp: Long = System.currentTimeMillis())

data class FindBookingByIdQuery(val bookingId: BookingId)

data class BookingUpdatedUpdate(val bookingId: String)
