package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

data class AddressDetails(
        var deliveryAddress: String? = null,
        var searchPhrase: String? = null,
        var x: String? = null,
        var y: String? = null,
        var complexName: String? = null,
        var businessName: String? = null,
        var buildingName: String? = null,
        var street: String? = null,
        var suburb: String? = null,
        var city: String? = null,
        var province: String? = null,
        var postalCode: String? = null)