package za.co.woolworths.financial.services.android.util.analytics

import android.app.Activity
import android.os.Bundle
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import za.co.woolworths.financial.services.android.models.WoolworthsApplication

class HuaweiManager {
    companion object {
        private var instance: HuaweiManager? = null

        fun getInstance(): HuaweiManager {
            if (instance == null) {
                instance = HuaweiManager()
            }
            return instance!!
        }

        fun logEvent(name: String, params: Bundle?) {
            getInstance().getAnalytics().onEvent(name, params)
        }

        fun setUserProperty(name: String, value: String?) {
            getInstance().getAnalytics().setUserProfile(name, value)
        }

        fun setUserId(id: String) {
            getInstance().getAnalytics().setUserId(id)
        }

        fun setCurrentScreen(activity: Activity, screenName: String?) {
            getInstance().getAnalytics().pageStart(screenName, activity.javaClass.simpleName)
        }
    }

    private var analyticsInstance: HiAnalyticsInstance? = null

    constructor() {
        val context = WoolworthsApplication.getAppContext()
        HiAnalytics.getInstance(context)
    }

    private fun getAnalytics(): HiAnalyticsInstance {
        if (analyticsInstance == null) {
            val context = WoolworthsApplication.getAppContext()
            analyticsInstance = HiAnalytics.getInstance(context)
        }
        return analyticsInstance!!
    }
}