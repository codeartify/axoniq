package ch.fitnesslab.customers.infrastructure.bexio

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class BexioCreateContactRequest(
    val nr: String? = null,
    @JsonProperty("name_1")
    val name1: String,
    @JsonProperty("name_2")
    val name2: String? = null,
    @JsonProperty("salutation_id")
    val salutationId: Int? = null,
    @JsonProperty("street_name")
    val streetName: String? = null,
    @JsonProperty("house_number")
    val houseNumber: String? = null,
    @JsonProperty("address_addition")
    val addressAddition: String? = null,
    val postcode: String? = null, // Correct field name for postal code
    val city: String? = null,
    @JsonProperty("country_id")
    val countryId: Int? = null,
    val mail: String? = null,
    @JsonProperty("mail_second")
    val mailSecond: String? = null,
    @JsonProperty("phone_fixed")
    val phoneFixed: String? = null,
    @JsonProperty("phone_mobile")
    val phoneMobile: String? = null,
    val fax: String? = null,
    val url: String? = null,
    @JsonProperty("language_id")
    val languageId: Int? = null,
    @JsonProperty("contact_type_id")
    val contactTypeId: Int, // Required field
    @JsonProperty("user_id")
    val userId: Int, // Required field
    @JsonProperty("owner_id")
    val ownerId: Int, // Required field
    @JsonProperty("contact_group_ids")
    val contactGroupIds: List<Int>? = null,
)
