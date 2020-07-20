package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

data class UpdateAddressDetailsRequestBody(
        var addressDetails: AddressDetails?,
        var productOfferingId: String?
)