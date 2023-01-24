package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
class DashRootCategories(

    @SerializedName("onDemandCategories")
    var onDemandCategories: @RawValue ArrayList<RootCategory>? = ArrayList(0),

    @SerializedName("response")
    var response: @RawValue Response? = null,

    @SerializedName("httpCode")
    var httpCode: Int? = null

) : Parcelable