package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Kunal Uttarwar on 15/3/21.
 */
@Parcelize
data class Promotions(
    @SerializedName("promotionalText")
    var promotionalText: String? = null,

    @SerializedName("searchTerm")
    var searchTerm: String? = null
) : Parcelable