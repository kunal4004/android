package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigApplyNowLinks(
        val storeCard: String?,
        val creditCard: String?,
        val personalLoan: String?,
        val applicationStatus: String?
) : Parcelable