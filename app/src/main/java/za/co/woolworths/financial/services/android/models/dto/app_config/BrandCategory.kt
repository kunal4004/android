package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BrandCategory(
    val brandName: String?,
    val externalImageRefV2: String?
) : Parcelable
