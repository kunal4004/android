package za.co.woolworths.financial.services.android.util.location

import android.util.Log

object Logger {

    var isLoggingEnabled = false

    fun logDebug(message: String?) {
        if (isLoggingEnabled) {
            Log.d("oneApp-location", "$message")
        }
    }
}
