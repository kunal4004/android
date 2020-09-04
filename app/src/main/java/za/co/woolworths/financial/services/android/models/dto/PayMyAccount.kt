package za.co.woolworths.financial.services.android.models.dto

import com.awfs.coordination.BuildConfig
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.Utils
import java.net.URLEncoder
import java.util.*

data class PayMyAccount(var minimumSupportedAppBuildNumber: String? = "0", private var addCardBaseUrl: String?="") {

    fun addCardUrl(): String {
        return addCardBaseUrl
                ?.replace("{{api_id}}", URLEncoder.encode(WoolworthsApplication.getApiId()?.toLowerCase(Locale.getDefault()), "UTF-8").toString())
                ?.replace("{{sha1}}", URLEncoder.encode(BuildConfig.SHA1, "UTF-8"))
                ?.replace("{{agent}}", "android") ?: ""
    }

    fun isFeatureEnabled(): Boolean? = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)  ?: false
}