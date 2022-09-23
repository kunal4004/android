package za.co.woolworths.financial.services.android.util.analytics

import android.app.Activity
import android.os.Bundle

class AnalyticsManager {
    // Note that Firebase Analytics will work on all variants, including Huawei
    // However, Huawei Analytics will work only for Huawei variants
    companion object {
        fun logEvent(name: String, params: Bundle?) {
            FirebaseManager.logEvent(name, params)
            HuaweiManager.logEvent(name, params)
        }

        fun setUserProperty(name: String, value: String?) {
            FirebaseManager.setUserProperty(name, value)
            HuaweiManager.setUserProperty(name, value)
        }

        fun setUserId(id: String) {
            FirebaseManager.setUserId(id)
            HuaweiManager.setUserId(id)
        }

        fun setCurrentScreen(activity: Activity, screenName: String?) {
            FirebaseManager.setCurrentScreen(activity, screenName)
            HuaweiManager.setCurrentScreen(activity, screenName)
        }
    }
}