package za.co.woolworths.financial.services.android.models.network

import za.co.woolworths.financial.services.android.ui.activities.maintenance.NetworkRuntimeExceptionViewController
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.net.SocketTimeoutException

class RetrofitException(private var url:String, private var exceptionCode: Any?, private var exceptionResponse:Any?) {
    fun show(): Boolean {
        return when {
            exceptionResponse is SocketTimeoutException -> {
                FirebaseManager.logException(exceptionResponse)
                NetworkRuntimeExceptionViewController().openSocketTimeOutDialog()
                true
            }
            (exceptionCode == 404) or (exceptionCode == 503) -> {
                if(!url.contains("mobileconfigs")) {
                    NetworkRuntimeExceptionViewController().openMaintenanceView()
                }
                true
            }
            else -> false
        }
    }
}