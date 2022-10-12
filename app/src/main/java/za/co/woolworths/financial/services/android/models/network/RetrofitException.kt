package za.co.woolworths.financial.services.android.models.network

import za.co.woolworths.financial.services.android.ui.activities.maintenance.NetworkRuntimeExceptionViewController
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_NOT_FOUND_404
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SERVICE_UNAVAILABLE_503
import java.net.SocketTimeoutException

class RetrofitException(private var url: String, private var exception: Any?) {
    fun show(): Boolean {
        return when {
            exception is SocketTimeoutException -> {
                NetworkRuntimeExceptionViewController().openSocketTimeOutDialog()
                true
            }
            (exception == HTTP_NOT_FOUND_404) or (exception == HTTP_SERVICE_UNAVAILABLE_503) -> {
                if (!url.contains("mobileconfigs")) {
                    if (exception == HTTP_SERVICE_UNAVAILABLE_503) NetworkRuntimeExceptionViewController().openWebViewErrorScreen(
                        "https://www.google.com/")
                    else
                        NetworkRuntimeExceptionViewController().openMaintenanceView()
                }
                true
            }
            else -> false
        }
    }
}