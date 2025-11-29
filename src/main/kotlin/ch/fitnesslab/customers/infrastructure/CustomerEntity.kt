package ch.fitnesslab.customers.infrastructure

import ch.fitnesslab.common.types.Salutation
import jakarta.persistence.*
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "customers")
class CustomerEntity(
    @Id
    @Column(name = "customer_id")
    val customerId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val salutation: Salutation,
    @Column(nullable = false)
    val firstName: String,
    @Column(nullable = false)
    val lastName: String,
    @Column(nullable = false)
    val dateOfBirth: LocalDate,
    @Column(nullable = false)
    val street: String,
    @Column(nullable = false)
    val houseNumber: String,
    @Column(nullable = false)
    val postalCode: String,
    @Column(nullable = false)
    val city: String,
    @Column(nullable = false)
    val country: String,
    @Column(nullable = false)
    val email: String,
    @Column(nullable = true)
    val phoneNumber: String?,
)
