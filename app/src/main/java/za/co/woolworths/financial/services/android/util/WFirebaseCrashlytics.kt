package za.co.woolworths.financial.services.android.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.lang.IllegalArgumentException

class WFirebaseCrashlytics {

    fun logException(e: Any?) {

        val firebaseCrashlytics = FirebaseCrashlytics.getInstance()

        when (e) {
            is IllegalArgumentException -> firebaseCrashlytics.log("E/TAG: $e")
            is Throwable -> firebaseCrashlytics.log("E/TAG: ${(e as? Throwable)?.message}}")
            is Exception -> firebaseCrashlytics.log("E/TAG: ${(e as? Exception)?.message}")
            is String -> firebaseCrashlytics.log(e.toString())
            else -> firebaseCrashlytics.log(e.toString())
        }
    }

    fun setString(key: String, value: String?) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value ?: "")
    }
}