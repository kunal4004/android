package za.co.woolworths.financial.services.android.models.network

import za.co.woolworths.financial.services.android.ui.activities.maintenance.NetworkRuntimeExceptionViewController
import java.net.SocketTimeoutException

class RetrofitException(private val exception: Any?) {
    fun show(): Boolean {
        return when (exception) {
            is SocketTimeoutException -> {
                NetworkRuntimeExceptionViewController().openSocketTimeOutDialog()
                true
            }
            equals(404), equals(503) -> {
                NetworkRuntimeExceptionViewController().openMaintenanceView()
                true
            }
            else -> false
        }
    }
}