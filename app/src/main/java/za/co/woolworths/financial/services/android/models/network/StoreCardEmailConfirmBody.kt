package za.co.woolworths.financial.services.android.models.network

import java.io.Serializable

class StoreCardEmailConfirmBody(var visionAccountNumber: String? = null, var deliveryMethod: String? = null, var storeName: String? = null, var storeAddress: String? = null, var province: String? = null, var city: String? = null, var suburb: String? = null, var street: String? = null, var complexName: String? = null, var businessName: String? = null, var postalCode: String? = null) : Serializable