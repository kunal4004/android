package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Store(var id: String?, var name: String? = null, @SerializedName("fulfillmentStores")
@Expose var fulfillmentStores: JsonElement?, var storeAddress: String?)