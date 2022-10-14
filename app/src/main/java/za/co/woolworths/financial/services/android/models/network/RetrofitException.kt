package za.co.woolworths.financial.services.android.models.network

import za.co.woolworths.financial.services.android.ui.activities.maintenance.NetworkRuntimeExceptionViewController
import java.net.SocketTimeoutException

class RetrofitException(private var url:String, private var exception: Any?) {
    fun show(): Boolean {
        return when {
            exception is SocketTimeoutException -> {
                NetworkRuntimeExceptionViewController().openSocketTimeOutDialog()
                true
            }
            (exception == 404) or (exception == 503) -> {
                if(!url.contains("mobileconfigs")) {
                    NetworkRuntimeExceptionViewController().openMaintenanceView()
                }
                true
            }
            else -> false
        }
    }
}