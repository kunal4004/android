package za.co.woolworths.financial.services.android.models.dto.account

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FicaModel(var refreshStatus: RefreshStatus, var httpCode: Int?) :
    Parcelable


@Parcelize
data class RefreshStatus( var refreshDue: Boolean , var appGuid:String) :
    Parcelable