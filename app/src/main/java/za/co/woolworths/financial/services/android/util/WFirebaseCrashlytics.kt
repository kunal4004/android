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

    fun setUserId(userId: String) {
        FirebaseCrashlytics.getInstance().setUserId(userId)
    }

    fun setCustomKeyValue(keyId: String, valueId: String) {
        FirebaseCrashlytics.getInstance().setCustomKey(keyId, valueId)
    }

    companion object {
        const val ETag = "E/TAG:"
    }
}