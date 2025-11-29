package ch.fitnesslab.customers.adapter.http

import ch.fitnesslab.common.types.Address
import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.Salutation
import ch.fitnesslab.customers.application.CustomerProjection
import ch.fitnesslab.customers.application.CustomerUpdatedUpdate
import ch.fitnesslab.customers.application.FindAllCustomersQuery
import ch.fitnesslab.customers.domain.commands.RegisterCustomerCommand
import ch.fitnesslab.customers.domain.commands.UpdateCustomerCommand
import ch.fitnesslab.generated.api.CustomersApi
import ch.fitnesslab.generated.model.CustomerRegistrationResponse
import ch.fitnesslab.generated.model.CustomerView
import ch.fitnesslab.generated.model.RegisterCustomerRequest
import ch.fitnesslab.generated.model.UpdateCustomerRequest
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Duration

@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val commandGateway: CommandGateway,
    private val customerProjection: CustomerProjection,
    private val queryGateway: QueryGateway,
) : CustomersApi {
    @PostMapping
    override fun registerCustomer(
        @RequestBody registerCustomerRequest: RegisterCustomerRequest,
    ): ResponseEntity<CustomerRegistrationResponse> {
        val customerId = CustomerId.generate()

        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllCustomersQuery(),
                ResponseTypes.multipleInstancesOf(CustomerView::class.java),
                ResponseTypes.instanceOf(CustomerUpdatedUpdate::class.java),
            )

        try {
            val command =
                RegisterCustomerCommand(
                    customerId = customerId,
                    salutation = registerCustomerRequest.salutation.let { Salutation.valueOf(it.name) },
                    firstName = registerCustomerRequest.firstName,
                    lastName = registerCustomerRequest.lastName,
                    dateOfBirth = registerCustomerRequest.dateOfBirth,
                    address =
                        Address(
                            street = registerCustomerRequest.address.street,
                            houseNumber = registerCustomerRequest.address.houseNumber,
                            postalCode = registerCustomerRequest.address.postalCode,
                            city = registerCustomerRequest.address.city,
                            country = registerCustomerRequest.address.country,
                        ),
                    email = registerCustomerRequest.email,
                    phoneNumber = registerCustomerRequest.phoneNumber,
                )
            commandGateway.sendAndWait<Any>(command)

            // Wait for projection update
            subscriptionQuery.updates().blockFirst(Duration.ofSeconds(5))

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomerRegistrationResponse(customerId.toString()))
        } finally {
            subscriptionQuery.close()
        }
    }

    @GetMapping("/{customerId}")
    override fun getCustomer(
        @PathVariable customerId: String,
    ): ResponseEntity<ch.fitnesslab.generated.model.CustomerView> {
        val customer = customerProjection.findById(CustomerId.from(customerId))
        return if (customer != null) {
            ResponseEntity.ok(
                customer.let {
                    CustomerView(
                        it.customerId,
                        it.salutation,
                        it.firstName,
                        it.lastName,
                        it.dateOfBirth,
                        it.address,
                        it.email,
                        it.phoneNumber,
                    )
                },
            )
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    override fun getAllCustomers(): ResponseEntity<List<CustomerView>> = ResponseEntity.ok(customerProjection.findAll())

    @PutMapping("/{customerId}")
    override fun updateCustomer(
        @PathVariable customerId: String,
        @RequestBody updateCustomerRequest: UpdateCustomerRequest,
    ): ResponseEntity<Unit> {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllCustomersQuery(),
                ResponseTypes.multipleInstancesOf(CustomerView::class.java),
                ResponseTypes.instanceOf(CustomerUpdatedUpdate::class.java),
            )

        try {
            val command =
                UpdateCustomerCommand(
                    customerId = CustomerId.from(customerId),
                    salutation = updateCustomerRequest.salutation.let { Salutation.valueOf(it.name) },
                    firstName = updateCustomerRequest.firstName,
                    lastName = updateCustomerRequest.lastName,
                    dateOfBirth = updateCustomerRequest.dateOfBirth,
                    address =
                        Address(
                            street = updateCustomerRequest.address.street,
                            houseNumber = updateCustomerRequest.address.houseNumber,
                            postalCode = updateCustomerRequest.address.postalCode,
                            city = updateCustomerRequest.address.city,
                            country = updateCustomerRequest.address.country,
                        ),
                    email = updateCustomerRequest.email,
                    phoneNumber = updateCustomerRequest.phoneNumber,
                )

            commandGateway.sendAndWait<Any>(command)

            // Wait for projection update
            subscriptionQuery.updates().blockFirst(Duration.ofSeconds(5))

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }
}
