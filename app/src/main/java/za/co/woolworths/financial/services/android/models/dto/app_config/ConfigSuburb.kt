package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigSuburb(
    @SerializedName("suburbDeliverable") var suburbDeliverable: Boolean? = null,
    @SerializedName("postalCode") var postalCode: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("id") var id: Int? = null,
    @SerializedName("fulfilmentTypes") var fulfilmentTypes: MutableList<ConfigFulfilmentTypes>? = null
) : Parcelable