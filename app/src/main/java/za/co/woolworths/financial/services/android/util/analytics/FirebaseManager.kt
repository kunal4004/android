package za.co.woolworths.financial.services.android.util.analytics

import android.app.Activity
import android.os.Bundle
import androidx.annotation.Size
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.WFirebaseCrashlytics

class FirebaseManager {

    companion object {
        private var instance: FirebaseManager? = null

        fun getInstance(): FirebaseManager {
            if (instance == null) {
                instance = FirebaseManager()
            }
            return instance!!
        }

        @JvmStatic
        fun logException(e: Any?) {
            WFirebaseCrashlytics().logException(e)
        }

        fun setCrashlyticsString(name: String, value: String?) {
            WFirebaseCrashlytics().setString(name, value)
        }

        fun logEvent(name: String, params: Bundle?) {
            getInstance().getAnalytics().logEvent(name, params)
        }

        fun setUserProperty(name: String, value: String?) {
            getInstance().getAnalytics().setUserProperty(name, value)
        }

        fun setUserId(id: String) {
            getInstance().getAnalytics().setUserId(id)
        }

        fun setCurrentScreen(activity: Activity, screenName: String?) {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, activity.javaClass.simpleName)
            getInstance().getAnalytics().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        }
    }

    private var remoteConfig: FirebaseRemoteConfig? = null
    private var analytics: FirebaseAnalytics? = null

    constructor() {
        val context = WoolworthsApplication.getAppContext()
        FirebaseApp.initializeApp(context)
    }

    private fun getAnalytics(): FirebaseAnalytics {
        if (analytics == null) {
            val context = WoolworthsApplication.getAppContext()
            analytics = FirebaseAnalytics.getInstance(context)
        }
        return analytics!!
    }
}