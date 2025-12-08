package ch.fitnesslab.billing.infrastructure.bexio

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class BexioInvoiceDto(
    val id: Int,
    @JsonProperty("document_nr")
    val documentNr: String,
    val title: String?,
    @JsonProperty("contact_id")
    val contactId: Int,
    @JsonProperty("contact_sub_id")
    val contactSubId: Int?,
    @JsonProperty("user_id")
    val userId: Int,
    @JsonProperty("project_id")
    val projectId: Int?,
    @JsonProperty("logopaper_id")
    val logopaperId: Int,
    @JsonProperty("language_id")
    val languageId: Int,
    @JsonProperty("bank_account_id")
    val bankAccountId: Int,
    @JsonProperty("currency_id")
    val currencyId: Int,
    @JsonProperty("payment_type_id")
    val paymentTypeId: Int,
    val header: String?,
    val footer: String?,
    @JsonProperty("total_gross")
    val totalGross: BigDecimal,
    @JsonProperty("total_net")
    val totalNet: BigDecimal,
    @JsonProperty("total_taxes")
    val totalTaxes: BigDecimal,
    @JsonProperty("total_received_payments")
    val totalReceivedPayments: BigDecimal,
    @JsonProperty("total_credit_vouchers")
    val totalCreditVouchers: BigDecimal,
    @JsonProperty("total_remaining_payments")
    val totalRemainingPayments: BigDecimal,
    val total: BigDecimal,
    @JsonProperty("total_rounding_difference")
    val totalRoundingDifference: Int,
    @JsonProperty("mwst_type")
    val mwstType: Int,
    @JsonProperty("mwst_is_net")
    val mwstIsNet: Boolean,
    @JsonProperty("show_position_taxes")
    val showPositionTaxes: Boolean,
    @JsonProperty("is_valid_from")
    val isValidFrom: String,
    @JsonProperty("is_valid_to")
    val isValidTo: String,
    @JsonProperty("contact_address")
    val contactAddress: String?,
    @JsonProperty("kb_item_status_id")
    val kbItemStatusId: Int,
    val reference: String?,
    @JsonProperty("api_reference")
    val apiReference: String?,
    @JsonProperty("viewed_by_client_at")
    val viewedByClientAt: String?,
    @JsonProperty("updated_at")
    val updatedAt: String,
    @JsonProperty("esr_id")
    val esrId: Int?,
    @JsonProperty("qr_invoice_id")
    val qrInvoiceId: Int?,
    @JsonProperty("template_slug")
    val templateSlug: String?,
    val taxs: List<Any> = emptyList(),
    @JsonProperty("network_link")
    val networkLink: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BexioCreateInvoiceRequest(
    @JsonProperty("contact_id")
    val contactId: Int,
    @JsonProperty("user_id")
    val userId: Int, // Required field
    @JsonProperty("is_valid_from")
    val isValidFrom: String,
    val title: String?,
    val positions: List<BexioInvoicePosition>,
    @JsonProperty("api_reference")
    val apiReference: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BexioInvoicePosition(
    @JsonProperty("type")
    val type: String = "KbPositionCustom",
    val text: String,
    @JsonProperty("unit_price")
    val unitPrice: BigDecimal,
    @JsonProperty("account_id")
    val accountId: Int,
    @JsonProperty("tax_id")
    val taxId: Int,
    val amount: BigDecimal = BigDecimal.ONE
)
