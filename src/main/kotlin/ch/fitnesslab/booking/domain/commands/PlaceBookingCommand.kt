package ch.fitnesslab.booking.domain.commands

import ch.fitnesslab.booking.domain.PurchasedProduct
import ch.fitnesslab.common.types.BookingId
import ch.fitnesslab.common.types.CustomerId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class PlaceBookingCommand(
    @TargetAggregateIdentifier
    val bookingId: BookingId,
    val payerCustomerId: CustomerId,
    val purchasedProducts: List<PurchasedProduct>,
)
