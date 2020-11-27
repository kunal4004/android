package za.co.woolworths.financial.services.android.util

import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import za.co.woolworths.financial.services.android.contracts.IFirebaseManager
import za.co.woolworths.financial.services.android.models.WoolworthsApplication

class FirebaseManager : IFirebaseManager {

    companion object {
        //Firebase forms part of base functionality
        //of the app. It's without a doubt going to be
        //used and therefor, initializing a singleton here is acceptable.
        private var instance = FirebaseManager()

        fun getInstance(): IFirebaseManager {
            return instance
        }

        fun logException(e: Any?) {
            WFirebaseCrashlytics().logException(e)
        }

        fun setCrashlyticsString(name: String, value: String?) {
            WFirebaseCrashlytics().setString(name, value)
        }
    }

    private var remoteConfig: FirebaseRemoteConfig? = null
    private var analytics: FirebaseAnalytics? = null

    constructor() {
        val context = WoolworthsApplication.getAppContext()
        FirebaseApp.initializeApp(context)
    }

    override fun getAnalytics(): FirebaseAnalytics {

        if (analytics == null) {
            val context = WoolworthsApplication.getAppContext()
            analytics = FirebaseAnalytics.getInstance(context)
        }
        return analytics!!
    }
}