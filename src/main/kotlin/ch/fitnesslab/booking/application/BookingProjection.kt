package ch.fitnesslab.booking.application

import ch.fitnesslab.booking.domain.BookingStatus
import ch.fitnesslab.booking.domain.PurchasedProduct
import ch.fitnesslab.booking.domain.events.BookingPlacedEvent
import ch.fitnesslab.booking.infrastructure.BookingEntity
import ch.fitnesslab.booking.infrastructure.BookingRepository
import ch.fitnesslab.common.types.BookingId
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import java.util.*
@ProcessingGroup("booking")
@Component
class BookingProjection(
    private val bookingRepository: BookingRepository,
    private val objectMapper: ObjectMapper
) {

    @EventHandler
    fun on(event: BookingPlacedEvent) {
        val entity = BookingEntity(
            bookingId = event.bookingId.value,
            payerCustomerId = event.payerCustomerId.value,
            status = event.status,
            purchasedProductsJson = objectMapper.writeValueAsString(event.purchasedProducts)
        )
        bookingRepository.save(entity)
    }

    fun findById(bookingId: BookingId): BookingView? =
        bookingRepository.findById(bookingId.value).map { it.toBookingView() }.orElse(null)

    fun findAll(): List<BookingView> =
        bookingRepository.findAll().map { it.toBookingView() }

    fun findByCustomerId(customerId: String): List<BookingView> =
        bookingRepository.findByPayerCustomerId(UUID.fromString(customerId)).map { it.toBookingView() }

    fun findByStatus(status: BookingStatus): List<BookingView> =
        bookingRepository.findByStatus(status).map { it.toBookingView() }

    private fun BookingEntity.toBookingView() = BookingView(
        bookingId = this.bookingId.toString(),
        payerCustomerId = this.payerCustomerId.toString(),
        status = this.status,
        purchasedProducts = objectMapper.readValue<List<PurchasedProduct>>(this.purchasedProductsJson)
    )
}

data class BookingView(
    val bookingId: String,
    val payerCustomerId: String,
    val status: BookingStatus,
    val purchasedProducts: List<PurchasedProduct>
)
