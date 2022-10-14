package za.co.woolworths.financial.services.android.models.network

import za.co.woolworths.financial.services.android.models.dto.NetworkErrorResponse
import za.co.woolworths.financial.services.android.ui.activities.maintenance.NetworkRuntimeExceptionViewController
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_NOT_FOUND_404
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SERVICE_UNAVAILABLE_503
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.net.SocketTimeoutException

class RetrofitException(
    private var url: String,
    private var exceptionCode: Any?,
    private var exceptionResponse: Any?,
) {
    fun show(): Boolean {
        return when {
            exceptionResponse is SocketTimeoutException -> {
                FirebaseManager.logException(exceptionResponse)
                NetworkRuntimeExceptionViewController().openSocketTimeOutDialog()
                true
            }
            (exceptionCode == HTTP_NOT_FOUND_404) -> {
                if (!url.contains("mobileconfigs")) {
                    NetworkRuntimeExceptionViewController().openMaintenanceView()
                }
                true
            }
            exceptionResponse is NetworkErrorResponse -> {
                val networkErrorResponse = exceptionResponse as NetworkErrorResponse
                if (networkErrorResponse.httpCode == HTTP_SERVICE_UNAVAILABLE_503) {
                    if (url.contains("mobileconfigs") || networkErrorResponse.redirectURL.isNullOrEmpty()) {
                        NetworkRuntimeExceptionViewController().openMaintenanceView()
                    } else {
                        NetworkRuntimeExceptionViewController().openWebViewErrorScreen(
                            networkErrorResponse.redirectURL)
                    }
                }
                true
            }
            else -> false
        }
    }
}