package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.Province

@Parcelize
data class ConfigLiquor(
    var regions: ArrayList<Province>,
    var suburbs: ArrayList<String>,
    var message: String,
    var noLiquorImgUrl: String
) : Parcelable