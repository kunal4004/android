package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

/**
 * Created by Kunal Uttarwar on 31/08/21.
 */
class ValidationError {
    @SerializedName("field")
    private var field: String? = null

    @SerializedName("message")
    private var message: String? = null

    fun getField(): String? {
        return field
    }

    fun setField(field: String?) {
        this.field = field
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }
}