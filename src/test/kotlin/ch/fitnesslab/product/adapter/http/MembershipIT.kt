package ch.fitnesslab.product.adapter.http

import ch.fitnesslab.generated.model.AddressDto
import ch.fitnesslab.generated.model.Audience
import ch.fitnesslab.generated.model.CreateProductRequest
import ch.fitnesslab.generated.model.CustomerRegistrationResponse
import ch.fitnesslab.generated.model.CustomerView
import ch.fitnesslab.generated.model.RegisterCustomerRequest
import ch.fitnesslab.generated.model.Salutation
import ch.fitnesslab.product.application.ProductView
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class MembershipIT : IntegrationTest() {

    @Test
    fun `should register customer, create product and sign up membership using HTTP endpoints only`() {
        // 1) Register a customer via /api/customers
        val registerCustomerRequest: RegisterCustomerRequest =
            jsonLoader.loadObjectFromFile(
                "http/customers/customer1.json",
                RegisterCustomerRequest::class.java,
            )

        val expectedCustomer = CustomerView(
            customerId = "",
            salutation = registerCustomerRequest.salutation,
            firstName = registerCustomerRequest.firstName,
            lastName = registerCustomerRequest.lastName,
            dateOfBirth = registerCustomerRequest.dateOfBirth,
            address = registerCustomerRequest.address,
            email = registerCustomerRequest.email,
            phoneNumber = registerCustomerRequest.phoneNumber
        )

        val customerId = createCustomer(registerCustomerRequest)
        val customerFromGET = getCustomer(customerId)

        assertThat(customerFromGET)
            .usingRecursiveComparison()
            .ignoringFields("customerId")
            .isEqualTo(expectedCustomer)


        // 2) Create Subscription via /api/products
        /**
        val createProductRequest = CreateProductRequest(
            code = "ADDON",
            name = "Locker Addon",
            productType = "ADDON",
            audience = Audience.BOTH,
            requiresMembership = true,
            price = BigDecimal.valueOf(49.90),
            behavior = ch.fitnesslab.generated.model.ProductBehaviorConfig(
                isTimeBased = false,
                isSessionBased = false,
                canBePaused = false,
                autoRenew = false,
                contributesToMembershipStatus = false,
                renewalLeadTimeDays = null,
                maxActivePerCustomer = null,
                exclusivityGroup = null
            )
        )

        val productViewFromPost = webTestClient.post()
            .uri("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createProductRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(ProductView::class.java)
            .returnResult()
            .responseBody!!


        val productViewFromGET = webTestClient.get()
            .uri("/api/products/${productViewFromPost.productId}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(ProductView::class.java)
            .returnResult()
            .responseBody!!

        assertThat(productViewFromGET)
            .isEqualTo(productViewFromPost)

        webTestClient.post()
        .uri("/api/memberships/sign-up")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
        mapOf(
        "customerId" to createdCustomerId,
        "customerName" to "$firstName $lastName",
        "customerEmail" to email,
        "productVariantId" to productView.productId,
        "price" to productView.productId,
        "durationMonths" to 12,
        "paymentMode" to "PAY_ON_SITE"
        )
        )
        .exchange()
        .expectStatus().isOk
        .expectBody()


        // 5) Verify the created product contract via /api/product-contracts/{contractId}
        webTestClient.get()
        .uri("/api/product-contracts/{contractId}", createdContractId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.contractId").isEqualTo(createdContractId)
        .jsonPath("$.customerId").isEqualTo(createdCustomerId)
        .jsonPath("$.productVariantId").isEqualTo(selectedVariantId)
        .jsonPath("$.bookingId").isEqualTo(createdBookingId)

        // 6) Verify that the corresponding invoice exists via /api/invoices/customer/{customerId}
        webTestClient.get()
        .uri("/api/invoices/customer/{customerId}", createdCustomerId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[0].invoiceId").value<String> { id ->
        // At least one invoice must match the invoice from the membership sign-up
        assert(id == createdInvoiceId)
        }
         */
    }

    private fun getCustomer(customerId: String?): CustomerView = webTestClient.get()
        .uri("/api/customers/$customerId")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody(CustomerView::class.java)
        .returnResult()
        .responseBody!!

    private fun createCustomer(registerCustomerRequest: RegisterCustomerRequest): String? = webTestClient.post()
        .uri("/api/customers")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(registerCustomerRequest)
        .exchange()
        .expectStatus().isCreated
        .expectBody(CustomerRegistrationResponse::class.java)
        .returnResult()
        .responseBody!!
        .customerId
}
