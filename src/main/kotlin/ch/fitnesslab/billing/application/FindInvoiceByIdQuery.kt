package ch.fitnesslab.billing.application

import ch.fitnesslab.common.types.InvoiceId

data class FindInvoiceByIdQuery(
    val invoiceId: InvoiceId,
)
