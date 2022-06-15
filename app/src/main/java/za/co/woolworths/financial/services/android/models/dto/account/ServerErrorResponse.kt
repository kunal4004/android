package za.co.woolworths.financial.services.android.models.dto.account

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ServerErrorResponse(
    val code: String = "",
    var desc: String = "",
    val stsParams: String = "",
    val message: String = "",
    val version: String = ""
) : Parcelable
