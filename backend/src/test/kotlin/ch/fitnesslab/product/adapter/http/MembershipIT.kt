package ch.fitnesslab.product.adapter.http

import ch.fitnesslab.billing.infrastructure.InvoiceEmailService
import ch.fitnesslab.generated.model.*
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import jakarta.mail.BodyPart
import jakarta.mail.Multipart
import jakarta.mail.Part
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.io.ByteArrayInputStream
import java.time.LocalDate

class MembershipIT : IntegrationTest() {
    @MockitoBean
    lateinit var mailSender: JavaMailSender

    @MockitoSpyBean
    lateinit var invoiceEmailService: InvoiceEmailService

    @Test
    fun `should register customer, create product, sign up membership and send invoice mail`() {
//        val realMailSender = JavaMailSenderImpl()
//        val mimeMessage: MimeMessage = realMailSender.createMimeMessage()
//        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)
//
//        // 1) Register a customer via /api/customers
//        val registerCustomerRequest: RegisterCustomerRequest =
//            jsonLoader.loadObjectFromFile(
//                "http/customers/customer1.json",
//                RegisterCustomerRequest::class.java,
//            )
//
//        val expectedCustomer =
//            CustomerView(
//                customerId = "", // ignored
//                salutation = registerCustomerRequest.salutation,
//                firstName = registerCustomerRequest.firstName,
//                lastName = registerCustomerRequest.lastName,
//                dateOfBirth = registerCustomerRequest.dateOfBirth,
//                address = registerCustomerRequest.address,
//                email = registerCustomerRequest.email,
//                phoneNumber = registerCustomerRequest.phoneNumber,
//            )
//
//        val customerIdWhoWantsToSignUpForMembership = createCustomer(registerCustomerRequest)
//        val customerFromGET = getCustomer(customerIdWhoWantsToSignUpForMembership)
//
//        assertThat(customerFromGET).usingRecursiveComparison().ignoringFields("customerId").isEqualTo(expectedCustomer)
//
//        // 2) Create Subscription via /api/products
//        val createProductRequest =
//            jsonLoader.loadObjectFromFile(
//                "http/products/subscription1.json",
//                CreateProductRequest::class.java,
//            )
//
//        val expectedProductView =
//            ProductView(
//                productId = "", // ignored
//                code = createProductRequest.code,
//                name = createProductRequest.name,
//                productType = createProductRequest.productType,
//                audience = createProductRequest.audience.name,
//                requiresMembership = createProductRequest.requiresMembership,
//                price = createProductRequest.price,
//                behavior =
//                    ProductBehaviorConfig(
//                        canBePaused = createProductRequest.behavior.canBePaused,
//                        renewalLeadTimeDays = createProductRequest.behavior.renewalLeadTimeDays,
//                        maxActivePerCustomer = createProductRequest.behavior.maxActivePerCustomer,
//                        durationInMonths = createProductRequest.behavior.durationInMonths,
//                        numberOfSessions = createProductRequest.behavior.numberOfSessions,
//                    ),
//            )
//        val subscriptionId = createProduct(createProductRequest)
//        val productViewFromGET = getProduct(subscriptionId)
//
//        assertThat(productViewFromGET)
//            .usingRecursiveComparison()
//            .ignoringFields("productId")
//            .isEqualTo(expectedProductView)
//
//        // 3) Sign up membership via /api/memberships/sign-up
//        val memberShip =
//            signUpForMemberShip(
//                customerId = customerIdWhoWantsToSignUpForMembership,
//                memberShipId = productViewFromGET.productId,
//            )
//
//        // 4) Verify InvoiceEmailService was called with correct InvoiceView and recipient
//        val invoiceCaptor = argumentCaptor<InvoiceView>()
//        val customerNameCaptor = argumentCaptor<String>()
//        val customerEmailCaptor = argumentCaptor<String>()
//
//        verify(invoiceEmailService).sendInvoiceEmail(
//            invoiceCaptor.capture(),
//            customerNameCaptor.capture(),
//            customerEmailCaptor.capture(),
//        )
//
//        val sentInvoice = invoiceCaptor.firstValue
//        val sentCustomerName = customerNameCaptor.firstValue
//        val sentCustomerEmail = customerEmailCaptor.firstValue
//
//        assertThat(sentCustomerEmail).isEqualTo(registerCustomerRequest.email)
//
//        assertThat(sentCustomerName).contains(registerCustomerRequest.firstName)
//
//        val expectedInvoice =
//            InvoiceView(
//                invoiceId = "",
//                bookingId = "",
//                customerId = customerIdWhoWantsToSignUpForMembership!!,
//                amount = productViewFromGET.price!!,
//                dueDate = LocalDate.now().plusDays(30),
//                status = InvoiceStatus.OPEN,
//                paidAt = null,
//                isInstallment = false,
//                installmentNumber = null,
//                customerName = customerFromGET.firstName + " " + customerFromGET.lastName,
//            )
//
//        assertThat(sentInvoice)
//            .usingRecursiveComparison()
//            .ignoringFields("invoiceId", "bookingId")
//            .isEqualTo(expectedInvoice)
//
//        val messageCaptor: ArgumentCaptor<MimeMessage> = ArgumentCaptor.forClass(MimeMessage::class.java)
//
//        verify(mailSender).send(messageCaptor.capture())
//
//        val sentMessage = messageCaptor.value
//
//        assertThat(sentMessage.from[0].toString()).isEqualTo("noreply@fitnesslab.com")
//        assertThat(sentMessage.allRecipients[0].toString()).isEqualTo(sentCustomerEmail)
//        assertThat(sentMessage.subject).startsWith("Your Invoice from FitnessLab - ")
//
//        val sentEmailBody = extractTextBody(sentMessage)
//        val expectedEmailPattern =
//            Regex(
//                """
//                (?s)Dear\s+${Regex.escape(sentCustomerName)},\s*
// .*Thank you for your business with FitnessLab!
// .*Please find attached your invoice details:
// .*Invoice Number:\s*${Regex.escape(sentInvoice.invoiceId)}
// .*Amount:\s*\$${Regex.escape(sentInvoice.amount.toPlainString())}
// .*Due Date:\s*${Regex.escape(sentInvoice.dueDate.format(DateTimeFormatter.ISO_DATE))}
// .*Status:\s*${sentInvoice.status}
// .*Please ensure payment is made by the due date\.
// .*If you have any questions, please don't hesitate to contact us\.
// .*Best regards,
// .*FitnessLab Team\s*
//                """.trimIndent(),
//            )
//
//        assertThat(sentEmailBody).matches { expectedEmailPattern.matches(it) }
//
//        val pdfText = getPDFContent(sentMessage)
//        val invoiceDate = sentInvoice.dueDate.minusDays(30).format(DateTimeFormatter.ISO_DATE)
//        val dueDate = sentInvoice.dueDate.format(DateTimeFormatter.ISO_DATE)
//        val amountStr = sentInvoice.amount.toPlainString() // "999.00"
//        val statusStr = sentInvoice.status.name // "OPEN"
//        val description =
//            if (sentInvoice.isInstallment) {
//                "Installment ${sentInvoice.installmentNumber ?: 1} - Membership Fee"
//            } else {
//                "Membership Fee"
//            }
//
//        val expectedPdfRegex =
//            Regex(
//                "(?s)" +
//                    ".*INVOICE.*" +
//                    "FitnessLab.*" +
//                    "Fitness Center.*" +
//                    "Invoice Number: ${sentInvoice.invoiceId}.*" +
//                    "Invoice Date:\\s+${Regex.escape(invoiceDate)}.*" +
//                    "Due Date:\\s+${Regex.escape(dueDate)}.*" +
//                    "Status:\\s+${Regex.escape(statusStr)}.*" +
//                    "Bill To:.*" +
//                    Regex.escape(sentCustomerName) + ".*" +
//                    Regex.escape(sentCustomerEmail) + ".*" +
//                    "Description\\s+Amount.*" +
//                    Regex.escape(description) + "\\s+\\$${Regex.escape(amountStr)}.*" +
//                    "Total Amount Due:\\s+\\$${Regex.escape(amountStr)}.*" +
//                    "Thank you for your business!.*" +
//                    "Please pay by the due date\\..*",
//            )
//
//        assertThat(pdfText).matches {
//            expectedPdfRegex.matches(it)
//        }
//
//        // 5) Pause membership contract via /api/product-contracts/{contractId}/pause
//        val pauseRequest =
//            PauseContractRequest(
//                startDate = LocalDate.parse("2023-06-01"),
//                endDate = LocalDate.parse("2023-06-30"), // 30 days (within 21–56)
//                reason = PauseContractRequest.Reason.MEDICAL,
//            )
//
//        // Verify contract is initially ACTIVE
//        val contractId = memberShip.contractId
//        val initialContract =
//            webTestClient
//                .get()
//                .uri("/api/product-contracts/$contractId")
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus()
//                .isOk
//                .expectBody(ProductContractDetailDto::class.java)
//                .returnResult()
//                .responseBody!!
//
//        assertThat(initialContract.status).isEqualTo("ACTIVE")
//
//        // Pause
//        pauseContract(contractId, pauseRequest)
//
//        val pausedContract = getContract(contractId)
//
//        val expectedPausedContract =
//            initialContract.copy(
//                status = "PAUSED",
//                pauseHistory =
//                    listOf(
//                        PauseHistoryEntryDto(
//                            pauseRange =
//                                DateRangeDto(
//                                    pauseRequest.startDate,
//                                    pauseRequest.endDate,
//                                ),
//                            reason = PauseReason.MEDICAL.toString(),
//                        ),
//                    ),
//                canBePaused = false,
//            )
//        assertThat(pausedContract)
//            .usingRecursiveComparison()
//            .isEqualTo(expectedPausedContract)
//
//        // 6) Resume membership contract via /api/product-contracts/{contractId}/resume
//        resume(contractId)
//
//        val actualResumedContract = getContract(contractId)
//
//        val pausedContractValidity = pausedContract.validity!!
//
//        val expectedResumedContract =
//            pausedContract.copy(
//                status = "ACTIVE",
//                canBePaused = true,
//                validity =
//                    DateRangeDto(
//                        pausedContractValidity.start,
//                        pausedContractValidity.end.plusDays(
//                            DAYS.between(
//                                pauseRequest.startDate,
//                                pauseRequest.endDate,
//                            ),
//                        ),
//                    ),
//            )
//
//        assertThat(actualResumedContract)
//            .usingRecursiveComparison()
//            .isEqualTo(expectedResumedContract)
    }

    private fun resume(contractId: String) {
        webTestClient
            .post()
            .uri("/api/product-contracts/$contractId/resume")
            .exchange()
            .expectStatus()
            .isOk
    }

    private fun getContract(contractId: String): ProductContractDetailDto =
        webTestClient
            .get()
            .uri("/api/product-contracts/$contractId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ProductContractDetailDto::class.java)
            .returnResult()
            .responseBody!!

    private fun pauseContract(
        contractId: String,
        pauseRequest: PauseContractRequest,
    ) {
        webTestClient
            .post()
            .uri("/api/product-contracts/$contractId/pause")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(pauseRequest)
            .exchange()
            .expectStatus()
            .isOk
    }

    private fun getPDFContent(sentMessage: MimeMessage): String {
        val pdfBytes = extractPdfAttachmentBytes(sentMessage)

        val pdfDoc = PdfDocument(PdfReader(ByteArrayInputStream(pdfBytes)))
        val pdfText =
            buildString {
                (1..pdfDoc.numberOfPages).forEach { page ->
                    append(PdfTextExtractor.getTextFromPage(pdfDoc.getPage(page)))
                    append('\n')
                }
            }
        pdfDoc.close()
        return pdfText
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
        memberShipId: String?,
    ): MembershipSignUpResultDto {
        val signupRequest =
            MembershipSignUpRequestDto(
                customerId = customerId!!,
                productVariantId = memberShipId!!,
                paymentMode = MembershipSignUpRequestDto.PaymentMode.PAY_ON_SITE,
                startDate = LocalDate.parse("2023-01-01"),
            )

        return webTestClient
            .post()
            .uri("/api/memberships/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signupRequest)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(MembershipSignUpResultDto::class.java)
            .returnResult()
            .responseBody!!
    }

    private fun getProduct(productId: String?): ProductView =
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

    private fun createProduct(createProductRequest: CreateProductRequest): String? =
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
            .expectBody(
                CustomerRegistrationResponse::class.java,
            ).returnResult()
            .responseBody!!
            .customerId

    private fun extractPdfAttachmentBytes(message: MimeMessage): ByteArray {
        val content = message.content
        require(content is Multipart) { "Expected multipart message, got ${content?.javaClass}" }

        for (i in 0 until content.count) {
            val part: BodyPart = content.getBodyPart(i)
            // Skip the text body, look for an attachment with PDF mime type
            if (Part.ATTACHMENT.equals(part.disposition, ignoreCase = true) ||
                part.contentType
                    .lowercase()
                    .contains("application/pdf")
            ) {
                val dataHandler = part.dataHandler
                return dataHandler.inputStream.readAllBytes()
            }
        }
        error("No PDF attachment found")
    }
}
