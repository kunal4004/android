package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConfigProductDetailsPage(
    var shareItemURITemplate: String,
    var shareItemMessage: String,
    var bnpl : BnplEmbeddedFinanceConfig? = null
) : Parcelable