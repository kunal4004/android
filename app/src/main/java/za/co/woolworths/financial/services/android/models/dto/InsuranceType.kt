package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class InsuranceType(
        @SerializedName("description")
        @Expose var description: String,
        @SerializedName("covered")
        @Expose var covered: Boolean,
        @SerializedName("effectiveDate")
        @Expose var effectiveDate: String) : Parcelable { constructor() : this("", false, "") }