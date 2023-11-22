package za.co.woolworths.financial.services.android.models.dto.shop

import android.os.Parcelable

import kotlinx.android.parcel.Parcelize

@Parcelize
data class Banner(
    var displayName: String? = null,
    var subTitle: String? = null,
    var navigationState: String? = null,
    var externalImageRefV2: String? = null,
    var filterContent: Boolean? = null,
    var subTitleText: String? = null,
    var descriptionText: String? = null
) : Parcelable