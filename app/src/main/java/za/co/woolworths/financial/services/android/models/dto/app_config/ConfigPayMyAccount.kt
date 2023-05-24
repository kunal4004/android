package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import com.awfs.coordination.BuildConfig
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.Utils
import java.net.URLEncoder
import java.util.*

@Parcelize
data class ConfigPayMyAccount(
    var minimumSupportedAppBuildNumber: Int,
    private var addCardBaseUrl: String?="",
    var paymentSuccessfulTitle:String?,
    var paymentSuccessfulDescription:String?,
    var enterPaymentAmountDialogFooterNote:String?,
    var payByCardFooterNote:String?
    ) : Parcelable {

    fun addCardUrl(productGroupCode: String): String {
        return addCardBaseUrl
                ?.replace("{{api_id}}", URLEncoder.encode(WoolworthsApplication.getApiId()?.toLowerCase(Locale.getDefault()), "UTF-8").toString())
                ?.replace("{{sha1}}", URLEncoder.encode(BuildConfig.SHA1, "UTF-8"))
                ?.replace("{{agent}}", "android")
                ?.replace("{{productGroupCode}}", productGroupCode.toLowerCase(Locale.getDefault())) ?: ""
    }

    fun isFeatureEnabled(): Boolean? = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)  ?: false
}