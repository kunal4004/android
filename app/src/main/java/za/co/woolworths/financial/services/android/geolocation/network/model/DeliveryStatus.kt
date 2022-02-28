package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.SerializedName

class DeliveryStatus {
    @SerializedName("01")
    private var _01: Boolean? = null

    @SerializedName("02")
    private var _02: Boolean? = null

    @SerializedName("07")
    private var _07: Boolean? = null
    fun get01(): Boolean? {
        return _01
    }

    fun set01(_01: Boolean?) {
        this._01 = _01
    }

    fun get02(): Boolean? {
        return _02
    }

    fun set02(_02: Boolean?) {
        this._02 = _02
    }

    fun get07(): Boolean? {
        return _07
    }

    fun set07(_07: Boolean?) {
        this._07 = _07
    }
}