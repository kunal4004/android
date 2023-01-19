package za.co.woolworths.financial.services.android.ui.activities.webview.data

import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.FicaRefresh
import za.co.woolworths.financial.services.android.util.ChromeClient

data class WebViewData(
    var mExternalLink: String,
    var downLoadUrl: String? = null,
    var downLoadMimeType: String? = null,
    var downLoadUserAgent: String? = null,
    var downLoadConntentDisposition: String? = null,
    var treatmentPlan: Boolean = false,
    var collectionsExitUrl: String? = null,
    var chromeClient: ChromeClient?,
    var ficaCanceled: Boolean = false,
    var isPetInsurance: Boolean = false,
    var fica: FicaRefresh? = null,
    var mustRedirectBlankTargetLinkToExternal: Boolean = false
)