package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BnplConfig(
    val componentTitle: String?,
    val componentDescription: String?,
    val infoLabelAvailableBalance: String?,
    val infoLabelEarnCashback: String?,
    val minimumSupportedAppBuildNumber: Int?,
    var isBnplEnabled: Boolean = false,
    var isBnplRequiredInThisVersion: Boolean = false,
    val payflex: WfsPaymentMethods?,
    val wfsPaymentMethods: MutableList<WfsPaymentMethods>
) : Parcelable

@Parcelize
data class WfsPaymentMethods(
    val instalmentCount: Int?,
    val productGroupCode: String?,
    val accountNumberBin: String?,
    val title: String,
    val description: String?,
    val cashbackPercentage: Float?,
    val infoTitle: String,
    val infoDescription: String,
    val infoFooterTitle: String?,
    val standalone : Standalone?,
    val infoDescriptionBoldParts : MutableList<String>,
    val infoFooterDescription: String?) : Parcelable

@Parcelize
data class Standalone(val description: String,
                      val descriptionHyperlinkPart : String?,
                      val descriptionBoldParts : MutableList<String>?) : Parcelable