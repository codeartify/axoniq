package ch.fitnesslab.product.adapter.http

import ch.fitnesslab.billing.application.InvoiceView
import ch.fitnesslab.billing.domain.InvoiceStatus
import ch.fitnesslab.billing.infrastructure.InvoiceEmailService
import ch.fitnesslab.billing.infrastructure.InvoicePdfGenerator
import ch.fitnesslab.generated.model.*
import jakarta.mail.BodyPart
import jakarta.mail.Multipart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.springframework.http.MediaType
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.time.LocalDate

class MembershipIT : IntegrationTest() {

    @MockitoBean
    lateinit var invoicePdfGenerator: InvoicePdfGenerator

    @MockitoBean
    lateinit var mailSender: JavaMailSender

    @MockitoSpyBean
    lateinit var invoiceEmailService: InvoiceEmailService

    @Test
    fun `should register customer, create product, sign up membership and send invoice mail`() {
        val realMailSender = JavaMailSenderImpl()
        val mimeMessage: MimeMessage = realMailSender.createMimeMessage()
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)

        `when`(invoicePdfGenerator.generateInvoicePdf(any(), any(), any()))
            .thenReturn("dummy-pdf".toByteArray())

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

        val expectedProductView = ProductView(
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
            .isEqualTo(expectedProductView)

        // 3) Sign up membership via /api/memberships/sign-up
        signUpForMemberShip(
            customerId = customerIdWhoWantsToSignUpForMembership,
            memberShipId = productViewFromGET.productId,
        )

        // 4) Verify InvoiceEmailService was called with correct InvoiceView and recipient
        val invoiceCaptor = argumentCaptor<InvoiceView>()
        val customerNameCaptor = argumentCaptor<String>()
        val customerEmailCaptor = argumentCaptor<String>()

        verify(invoiceEmailService).sendInvoiceEmail(
            invoiceCaptor.capture(),
            customerNameCaptor.capture(),
            customerEmailCaptor.capture(),
        )

        val sentInvoice = invoiceCaptor.firstValue
        val sentCustomerName = customerNameCaptor.firstValue
        val sentCustomerEmail = customerEmailCaptor.firstValue

        assertThat(sentCustomerEmail)
            .isEqualTo(registerCustomerRequest.email)

        assertThat(sentCustomerName)
            .contains(registerCustomerRequest.firstName)

        val expectedInvoice = InvoiceView(
            invoiceId = "",
            bookingId = "",
            customerId = customerIdWhoWantsToSignUpForMembership!!,
            amount = productViewFromGET.price!!,
            dueDate = LocalDate.now().plusDays(30),
            status = InvoiceStatus.OPEN,
            paidAt = null,
            isInstallment = false,
            installmentNumber = null,
            customerName = customerFromGET.firstName + " " + customerFromGET.lastName,
        )

        assertThat(sentInvoice).usingRecursiveComparison()
            .ignoringFields("invoiceId", "bookingId")
            .isEqualTo(expectedInvoice)

        val messageCaptor: ArgumentCaptor<MimeMessage> =
            ArgumentCaptor.forClass(MimeMessage::class.java)

        verify(mailSender).send(messageCaptor.capture())

        val sentMessage = messageCaptor.value

        assertThat(sentMessage.from[0].toString()).isEqualTo("noreply@fitnesslab.com")
        assertThat(sentMessage.allRecipients[0].toString()).isEqualTo(sentCustomerEmail)
        assertThat(sentMessage.subject).startsWith("Your Invoice from FitnessLab - ")

        val sentEmailBody = extractTextBody(sentMessage)
        val expectedEmailPattern = Regex(
            "(?s)^Dear .*," +
                    ".*Thank you for your business with FitnessLab!" +
                    ".*Please find attached your invoice details:" +
                    ".*Invoice Number: .*" +          // <-- ignore concrete invoice number
                    ".*Amount: .*" +
                    ".*Due Date: .*" +
                    ".*Status: .*" +
                    ".*Please ensure payment is made by the due date\\." +
                    ".*If you have any questions, please don't hesitate to contact us\\." +
                    ".*Best regards," +
                    ".*FitnessLab Team\\s*$"
        )

        assertThat(sentEmailBody).matches { expectedEmailPattern.matches(it) }
    }

    private fun extractTextBody(message: MimeMessage): String {
        val content = message.content
        return when (content) {
            is String -> content
            is MimeMultipart -> extractTextFromMultipart(content)
            is Multipart -> extractTextFromMultipart(content)
            else -> error("Unsupported message content type: ${content?.javaClass}")
        }
    }

    private fun extractTextFromMultipart(multipart: Multipart): String {
        for (i in 0 until multipart.count) {
            val part: BodyPart = multipart.getBodyPart(i)
            val partContent = part.content

            if (partContent is String) {
                return partContent
            }

            if (partContent is Multipart) {
                return extractTextFromMultipart(partContent)
            }
        }
        error("No text body found in multipart message")
    }

    private fun signUpForMemberShip(
        customerId: String?,
        memberShipId: String?
    ): ByteArray? {
        val signupRequest = MembershipSignUpRequestDto(
            customerId = customerId!!,
            productVariantId = memberShipId!!,
            paymentMode = MembershipSignUpRequestDto.PaymentMode.PAY_ON_SITE,
            startDate = LocalDate.parse("2023-01-01"),
        )

        return webTestClient.post()
            .uri("/api/memberships/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signupRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()
            .responseBody
    }

    private fun getProduct(productId: String?): ProductView = webTestClient
        .get()
        .uri("/api/products/$productId")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(ProductView::class.java)
        .returnResult()
        .responseBody!!

    private fun createProduct(createProductRequest: CreateProductRequest): String? = webTestClient
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
