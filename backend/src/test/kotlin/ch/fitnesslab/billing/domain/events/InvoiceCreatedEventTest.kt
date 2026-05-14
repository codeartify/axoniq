package ch.fitnesslab.billing.domain.events

import org.axonframework.conversion.jackson.JacksonConverter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InvoiceCreatedEventTest {
    @Test
    fun `deserializes legacy null installment flag`() {
        val json =
            """
            {
              "invoiceId": {"value": "00000000-0000-0000-0000-000000000001"},
              "bookingId": {"value": "00000000-0000-0000-0000-000000000002"},
              "customerId": {"value": "00000000-0000-0000-0000-000000000003"},
              "productId": {"value": "00000000-0000-0000-0000-000000000004"},
              "amount": 120.00,
              "dueDate": {"value": "2026-05-14"},
              "status": "OPEN",
              "isInstallment": null,
              "installmentNumber": null
            }
            """.trimIndent()

        val result = JacksonConverter().convert(json.toByteArray(), InvoiceCreatedEvent::class.java)

        assertEquals(false, result?.isInstallment == true)
        assertEquals(null, result?.installmentNumber)
    }
}
