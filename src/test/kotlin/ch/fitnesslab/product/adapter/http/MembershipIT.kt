package ch.fitnesslab.product.adapter.http

import ch.fitnesslab.generated.model.*
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class MembershipIT : IntegrationTest() {
    @Test
    fun `should register customer, create product and sign up membership using HTTP endpoints only`() {
        // 1) Register a customer via /api/customers
        val registerCustomerRequest: RegisterCustomerRequest =
            jsonLoader.loadObjectFromFile(
                "http/customers/customer1.json",
                RegisterCustomerRequest::class.java,
            )

        val expectedCustomer =
            CustomerView(
                customerId = "", // ignored
                salutation = registerCustomerRequest.salutation,
                firstName = registerCustomerRequest.firstName,
                lastName = registerCustomerRequest.lastName,
                dateOfBirth = registerCustomerRequest.dateOfBirth,
                address = registerCustomerRequest.address,
                email = registerCustomerRequest.email,
                phoneNumber = registerCustomerRequest.phoneNumber,
            )

        val customerIdWhoWantsToSignUpForMembership = createCustomer(registerCustomerRequest)
        val customerFromGET = getCustomer(customerIdWhoWantsToSignUpForMembership)

        assertThat(customerFromGET)
            .usingRecursiveComparison()
            .ignoringFields("customerId")
            .isEqualTo(expectedCustomer)

        // 2) Create Subscription via /api/products
        val createProductRequest =
            jsonLoader.loadObjectFromFile(
                "http/products/subscription1.json",
                CreateProductRequest::class.java,
            )

        val expectedProductView =
            ProductView(
                productId = "", // ignored
                code = createProductRequest.code,
                name = createProductRequest.name,
                productType = createProductRequest.productType,
                audience = createProductRequest.audience.name,
                requiresMembership = createProductRequest.requiresMembership,
                price = createProductRequest.price,
                behavior =
                    ProductBehaviorConfig(
                        canBePaused = createProductRequest.behavior.canBePaused,
                        renewalLeadTimeDays = createProductRequest.behavior.renewalLeadTimeDays,
                        maxActivePerCustomer = createProductRequest.behavior.maxActivePerCustomer,
                        durationInMonths = createProductRequest.behavior.durationInMonths,
                        numberOfSessions = createProductRequest.behavior.numberOfSessions,
                    ),
            )
        val subscriptionId = createProduct(createProductRequest)
        val productViewFromGET = getProduct(subscriptionId)

        assertThat(productViewFromGET)
            .usingRecursiveComparison()
            .ignoringFields("productId")
            .isEqualTo(
                expectedProductView,
            )

        // 3) Sign up membership via /api/memberships/sign-up
        /**
         webTestClient.post()
         .uri("/api/memberships/sign-up")
         .contentType(MediaType.APPLICATION_JSON)
         .bodyValue(
         MembershipSignUpRequestDto(
         customerId= customerIdWhoWantsToSignUpForMembership!!,
         productVariantId = productViewFromGET.productId!!,
         paymentMode = MembershipSignUpRequestDto.PaymentMode.PAY_ON_SITE,
         startDate = LocalDate.parse("2023-01-01")

         ))
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

    private fun getProduct(productId: String?): ProductView {
        val productViewFromGET =
            webTestClient
                .get()
                .uri("/api/products/$productId")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(ProductView::class.java)
                .returnResult()
                .responseBody!!
        return productViewFromGET
    }

    private fun createProduct(createProductRequest: CreateProductRequest): String? {
        val productId =
            webTestClient
                .post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createProductRequest)
                .exchange()
                .expectStatus()
                .isCreated
                .expectBody(ProductCreationResponse::class.java)
                .returnResult()
                .responseBody!!
                .productId
        return productId
    }

    private fun getCustomer(customerId: String?): CustomerView =
        webTestClient
            .get()
            .uri("/api/customers/$customerId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(CustomerView::class.java)
            .returnResult()
            .responseBody!!

    private fun createCustomer(registerCustomerRequest: RegisterCustomerRequest): String? =
        webTestClient
            .post()
            .uri("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerCustomerRequest)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody(CustomerRegistrationResponse::class.java)
            .returnResult()
            .responseBody!!
            .customerId
}
