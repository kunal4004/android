package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class InsuranceType(
        @SerializedName("description")
        @Expose var description: String,
        @SerializedName("covered")
        @Expose var covered: Boolean,
        @SerializedName("effectiveDate")
        @Expose var effectiveDate: String) : Serializable {
    constructor() : this("", false, "")
}