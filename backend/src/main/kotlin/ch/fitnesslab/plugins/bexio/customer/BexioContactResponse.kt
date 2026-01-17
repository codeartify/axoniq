package ch.fitnesslab.plugins.bexio.customer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class BexioContactResponse(
    val id: Int,
    val nr: String?,
    @JsonProperty("name_1")
    val name1: String,
    @JsonProperty("name_2")
    val name2: String?,
    @JsonProperty("salutation_id")
    val salutationId: Int?,
    @JsonProperty("title_id")
    val titleId: Int?,
    @JsonProperty("street_name")
    val streetName: String?,
    @JsonProperty("house_number")
    val houseNumber: String?,
    @JsonProperty("address_addition")
    val addressAddition: String?,
    @JsonProperty("postal_code")
    val postalCode: String?,
    val city: String?,
    @JsonProperty("country_id")
    val countryId: Int?,
    val mail: String?,
    @JsonProperty("mail_second")
    val mailSecond: String?,
    @JsonProperty("phone_fixed")
    val phoneFixed: String?,
    @JsonProperty("phone_mobile")
    val phoneMobile: String?,
    val fax: String?,
    val url: String?,
    @JsonProperty("language_id")
    val languageId: Int?,
    @JsonProperty("contact_type_id")
    val contactTypeId: Int?,
    @JsonProperty("contact_group_ids")
    val contactGroupIds: String?,
    @JsonProperty("contact_sector_id")
    val contactSectorId: Int?,
)
