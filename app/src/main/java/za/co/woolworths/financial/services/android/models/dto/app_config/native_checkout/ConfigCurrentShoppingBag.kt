package za.co.woolworths.financial.services.android.models.dto.app_config.native_checkout

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Kunal Uttarwar on 11/10/21.
 */
@Parcelize
data class ConfigCurrentShoppingBag(
    val isEnabled: Boolean,
    val title: String,
    val description: String
) : Parcelable