package ch.fitnesslab.booking.infrastructure

import ch.fitnesslab.booking.domain.BookingStatus
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "bookings")
class BookingEntity(
    @Id
    @Column(name = "booking_id")
    val bookingId: UUID,

    @Column(name = "payer_customer_id", nullable = false)
    val payerCustomerId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: BookingStatus,

    @Column(name = "purchased_products_json", nullable = false, columnDefinition = "TEXT")
    val purchasedProductsJson: String
)
