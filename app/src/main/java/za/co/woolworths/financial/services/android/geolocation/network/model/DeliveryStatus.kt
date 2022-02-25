package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DeliveryStatus {
    @SerializedName("01")
    @Expose
    private var _01: Boolean? = null
    fun get01(): Boolean? {
        return _01
    }

    fun set01(_01: Boolean?) {
        this._01 = _01
    }
}