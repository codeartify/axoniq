package ch.fitnesslab.common.types

data class Address(
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val country: String,
) {
    init {
        require(street.isNotBlank()) { "Street cannot be blank" }
        require(houseNumber.isNotBlank()) { "House number cannot be blank" }
        require(postalCode.isNotBlank()) { "Postal code cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        require(country.isNotBlank()) { "Country cannot be blank" }
    }
}
