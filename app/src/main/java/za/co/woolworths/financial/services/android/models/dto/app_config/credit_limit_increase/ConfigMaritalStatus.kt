package za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class ConfigMaritalStatus(
    var statusId: Int = 0,
    var statusDesc: String?
): Parcelable, Serializable
