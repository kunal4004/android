package za.co.woolworths.financial.services.android.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

class WFirebaseCrashlytics {

    fun logException(e: Any?) {

        val crashlytics = FirebaseCrashlytics.getInstance()

        when (e) {
            is Throwable -> crashlytics.recordException(e)
            is Exception -> crashlytics.recordException(e)
            is String -> crashlytics.log("$ETag $e")
            else -> crashlytics.log(e.toString())
        }
    }

    fun setString(key: String, value: String?) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value ?: "")
    }

    companion object {
        const val ETag = "E/TAG:"
    }
}