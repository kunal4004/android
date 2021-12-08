package za.co.woolworths.financial.services.android.ui.vto.utils

import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.Utils

class VirtualTryOnUtil {
    companion object {

        fun isVtoConfigAvailable() =
            Utils.isFeatureEnabled(WoolworthsApplication.getInstance()?.virtualTryOn?.minimumSupportedAppBuildNumber)
                ?: false

    }
}