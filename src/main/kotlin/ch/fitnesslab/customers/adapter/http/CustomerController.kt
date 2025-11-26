package ch.fitnesslab.customers.adapter.http

import ch.fitnesslab.common.types.Address
import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.Salutation
import ch.fitnesslab.customers.application.CustomerProjection
import ch.fitnesslab.customers.application.CustomerView
import ch.fitnesslab.customers.domain.commands.RegisterCustomerCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.concurrent.CompletableFuture

@RestController
@CrossOrigin
@RequestMapping("/api/customers")
class CustomerController(
    private val commandGateway: CommandGateway,
    private val customerProjection: CustomerProjection
) {

    @PostMapping
    fun registerCustomer(@RequestBody request: RegisterCustomerRequest): ResponseEntity<CustomerRegistrationResponse?> {
        val customerId = CustomerId.generate()

        val command = RegisterCustomerCommand(
            customerId = customerId,
            salutation = request.salutation,
            firstName = request.firstName,
            lastName = request.lastName,
            dateOfBirth = request.dateOfBirth,
            address = Address(
                street = request.address.street,
                houseNumber = request.address.houseNumber,
                postalCode = request.address.postalCode,
                city = request.address.city,
                country = request.address.country
            ),
            email = request.email,
            phoneNumber = request.phoneNumber
        )
      commandGateway.sendAndWait<Any>(command)


        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CustomerRegistrationResponse(customerId.toString()))

    }

    @GetMapping("/{customerId}")
    fun getCustomer(@PathVariable customerId: String): ResponseEntity<CustomerView> {
        val customer = customerProjection.findById(CustomerId.from(customerId))
        return if (customer != null) {
            ResponseEntity.ok(customer)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    fun getAllCustomers(): ResponseEntity<List<CustomerView>> {
        return ResponseEntity.ok(customerProjection.findAll())
    }
}

data class RegisterCustomerRequest(
    val salutation: Salutation,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val address: AddressDto,
    val email: String,
    val phoneNumber: String?
)

data class AddressDto(
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val country: String
)

data class CustomerRegistrationResponse(
    val customerId: String? = null,
    val error: String? = null
)
