package ch.fitnesslab.booking.infrastructure

import ch.fitnesslab.booking.domain.BookingStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BookingRepository : JpaRepository<BookingEntity, UUID> {
    fun findByPayerCustomerId(payerCustomerId: UUID): List<BookingEntity>
    fun findByStatus(status: BookingStatus): List<BookingEntity>
}
