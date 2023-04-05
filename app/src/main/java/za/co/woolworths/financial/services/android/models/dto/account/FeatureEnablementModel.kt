package za.co.woolworths.financial.services.android.models.dto.account

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class FeatureEnablementModel(
    var featureEnabled: List<FeatureEnabled>,
    var httpCode: Int?
) : Parcelable


@Parcelize
data class FeatureEnabled(var featureName: String, var enabled: Boolean) :
    Parcelable
