package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConnectOnline(
    @SerializedName("isFreeSimAvailable") val isFreeSimAvailable: Boolean,
    @SerializedName("freeSimTextMsg") val freeSimTextMsg: String
) : Parcelable