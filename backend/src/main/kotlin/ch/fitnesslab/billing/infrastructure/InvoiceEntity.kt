package ch.fitnesslab.billing.infrastructure

import ch.fitnesslab.billing.domain.InvoiceStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "invoices")
class InvoiceEntity(
    @Id
    @Column(name = "invoice_id")
    val invoiceId: UUID,
    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,
    @Column(name = "booking_id", nullable = false)
    val bookingId: UUID,
    @Column(name = "product_variant_id", nullable = true)
    val productVariantId: UUID?,
    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,
    @Column(name = "due_date", nullable = false)
    val dueDate: LocalDate,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: InvoiceStatus,
    @Column(name = "is_installment", nullable = false)
    val isInstallment: Boolean,
    @Column(name = "installment_number", nullable = true)
    val installmentNumber: Int?,
    @Column(name = "paid_at", nullable = true)
    val paidAt: Instant?,
)
