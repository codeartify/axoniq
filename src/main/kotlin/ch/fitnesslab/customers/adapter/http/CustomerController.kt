package ch.fitnesslab.customers.adapter.http

import ch.fitnesslab.customers.application.CustomerProjection
import ch.fitnesslab.customers.application.use_case.RegisterCustomerUseCase
import ch.fitnesslab.customers.application.use_case.UpdateCustomerUseCase
import ch.fitnesslab.customers.domain.commands.RegisterCustomerCommand
import ch.fitnesslab.customers.domain.commands.UpdateCustomerCommand
import ch.fitnesslab.customers.domain.value.*
import ch.fitnesslab.customers.infrastructure.CustomerEntity
import ch.fitnesslab.domain.value.Address
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.Salutation
import ch.fitnesslab.generated.api.CustomersApi
import ch.fitnesslab.generated.model.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import ch.fitnesslab.generated.model.Salutation as DomainSalutation

@RestController
class CustomerController(
    private val customerProjection: CustomerProjection,
    private val registerCustomerUseCase: RegisterCustomerUseCase,
    private val updateCustomerUseCase: UpdateCustomerUseCase,
) : CustomersApi {
    override fun registerCustomer(registerCustomerRequest: RegisterCustomerRequest): ResponseEntity<CustomerRegistrationResponse> {
        val command = toRegisterCommand(CustomerId.generate(), registerCustomerRequest)

        val customerId = registerCustomerUseCase.execute(command)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CustomerRegistrationResponse(customerId.toString()))
    }

    override fun getCustomer(customerId: String): ResponseEntity<CustomerView> =
        customerProjection
            .findById(CustomerId.from(customerId))
            ?.let(::toViewModel)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    override fun getAllCustomers(): ResponseEntity<List<CustomerView>> =
        ResponseEntity.ok(
            customerProjection
                .findAll()
                .map { toViewModel(it) },
        )

    override fun updateCustomer(
        customerId: String, updateCustomerRequest: UpdateCustomerRequest,
    ): ResponseEntity<Unit> {
        val command = toUpdateCustomerCommand(customerId, updateCustomerRequest)

        updateCustomerUseCase.execute(command)
        return ResponseEntity.ok().build()
    }

    private fun toRegisterCommand(
        customerId: CustomerId,
        request: RegisterCustomerRequest,
    ): RegisterCustomerCommand {
        val address = request.address

        return RegisterCustomerCommand(
            customerId = customerId,
            salutation = request.salutation.let { Salutation.valueOf(it.name) },
            firstName = FirstName.of(request.firstName),
            lastName = LastName.of(request.lastName),
            dateOfBirth = DateOfBirth.of(request.dateOfBirth),
            address =
                Address.of(
                    address.street,
                    address.houseNumber,
                    address.postalCode,
                    address.city,
                    address.country,
                ),
            email = EmailAddress.of(request.email),
            phoneNumber = PhoneNumber.of(request.phoneNumber),
        )
    }

    private fun toUpdateCustomerCommand(
        customerId: String,
        request: UpdateCustomerRequest,
    ): UpdateCustomerCommand {
        val address = request.address
        return UpdateCustomerCommand(
            customerId = CustomerId.from(customerId),
            salutation = request.salutation.let { Salutation.valueOf(it.name) },
            firstName = FirstName.of(request.firstName),
            lastName = LastName.of(request.lastName),
            dateOfBirth = DateOfBirth.of(request.dateOfBirth),
            address =
                Address.of(
                    address.street,
                    address.houseNumber,
                    address.postalCode,
                    address.city,
                    address.country,
                ),
            email = EmailAddress.of(request.email),
            phoneNumber = PhoneNumber.of(request.phoneNumber),
        )
    }

    private fun toViewModel(entity: CustomerEntity): CustomerView =
        CustomerView(
            customerId = entity.customerId,
            salutation = DomainSalutation.forValue(entity.salutation),
            firstName = entity.firstName,
            lastName = entity.lastName,
            dateOfBirth = entity.dateOfBirth,
            address =
                AddressDto(
                    street = entity.street,
                    houseNumber = entity.houseNumber,
                    postalCode = entity.postalCode,
                    city = entity.city,
                    country = entity.country,
                ),
            email = entity.email,
            phoneNumber = entity.phoneNumber,
        )
}
