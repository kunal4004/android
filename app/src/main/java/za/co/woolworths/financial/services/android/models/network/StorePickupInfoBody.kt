package za.co.woolworths.financial.services.android.models.network

import com.google.gson.annotations.SerializedName

class StorePickupInfoBody {

    @SerializedName("primaryContactNo")
    var primaryContactNo: String? = null

    @SerializedName("storeId")
    var storeId: String? = null

    @SerializedName("vehicleModel")
    var vehicleModel: String? = null

    @SerializedName("vehicleColour")
    var vehicleColour: String? = null

    @SerializedName("vehicleRegistration")
    var vehicleRegistration: String? = null

    @SerializedName("firstName")
    var firstName: String? = null

    @SerializedName("taxiOpted")
    var taxiOpted: Boolean = false

    @SerializedName("navSuburbId")
    var navSuburbId: String = ""
}
