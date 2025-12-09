package ch.fitnesslab.billing.application

import ch.fitnesslab.domain.value.InvoiceId

data class FindInvoiceByIdQuery(
    val invoiceId: InvoiceId,
)
