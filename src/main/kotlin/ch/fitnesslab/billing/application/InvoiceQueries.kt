package ch.fitnesslab.billing.application

import ch.fitnesslab.common.types.InvoiceId

data class FindAllInvoicesQuery(
    val timestamp: Long = System.currentTimeMillis(),
)

data class FindInvoiceByIdQuery(
    val invoiceId: InvoiceId,
)

data class InvoiceUpdatedUpdate(
    val invoiceId: String,
)
