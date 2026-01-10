package ch.fitnesslab.domain.value

data class Address(
    val street: Street,
    val houseNumber: HouseNumber,
    val postalCode: PostalCode,
    val city: City,
    val country: Country,
) {
    companion object {
        fun of(
            street: String,
            houseNumber: String,
            postalCode: String,
            city: String,
            country: String,
        ): Address =
            Address(
                street = Street.of(street),
                houseNumber = HouseNumber.of(houseNumber),
                postalCode = PostalCode.of(postalCode),
                city = City.of(city),
                country = Country.of(country),
            )
    }

    override fun toString(): String = "${street.value} ${houseNumber.value}, ${postalCode.value} ${city.value}, ${country.value}"
}
