package za.co.woolworths.financial.services.android.geolocation.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize


@Keep
@Parcelize
data class MapData(
    val latitude: Double?,
    val longitude: Double?,
    val isAddAddress: Boolean?,
) : Parcelable
