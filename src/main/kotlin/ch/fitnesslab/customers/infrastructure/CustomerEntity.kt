package ch.fitnesslab.customers.infrastructure

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "customers")
class CustomerEntity(
    @Id
    var customerId: String,
    var salutation: String,
    var firstName: String,
    var lastName: String,
    var dateOfBirth: LocalDate,
    var street: String,
    var houseNumber: String,
    var postalCode: String,
    var city: String,
    var country: String,
    var email: String,
    var phoneNumber: String? = null,
    var bexioContactId: Int? = null,
)
