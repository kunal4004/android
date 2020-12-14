package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

data class BookingAddress(
        var nameSurname: String? = null,
        var telWork: String? = null,
        var telCell: String? = null,
        var province: String? = null,
        var city: String? = null,
        var suburb: String? = null,
        var deliverTo: String? = null,
        var businessName: String? = null,
        var buildingName: String? = null,
        var street: String? = null,
        var complexName: String? = null,
        var postalCode: String? = null,
        var idNumber: String? = null,
        var isThirdPartyRecipient: Boolean? = false
)