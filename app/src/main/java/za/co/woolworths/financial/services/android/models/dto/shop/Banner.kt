package za.co.woolworths.financial.services.android.models.dto.shop

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Banner(
    @SerializedName("displayName")
    var displayName: String? = null,

    @SerializedName("navigationState")
    var navigationState: String? = null,

    @SerializedName("externalImageRefV2")
    var externalImageRefV2: String? = null,

    @SerializedName("filterContent")
    var filterContent: Boolean? = null
): Parcelable