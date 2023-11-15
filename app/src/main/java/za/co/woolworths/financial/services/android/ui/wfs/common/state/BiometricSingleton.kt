package za.co.woolworths.financial.services.android.ui.wfs.common.state

import android.app.Activity
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.Utils

enum class CurrentScreenType {
    BOTTOM_NAVIGATION_ACTIVITY, OTHERS
}

class BiometricSingleton {
    // LiveData to observe the current activity
    var currentScreenType : CurrentScreenType? = null
    var wasBiometricAuthenticationScreenActive = false

    // Use this method to set the current activity in the ViewModel
    fun setCurrentActivity(activity: Activity?) {
        currentScreenType = when (activity) {
            is MyAccountActivity, is BottomNavigationActivity -> CurrentScreenType.BOTTOM_NAVIGATION_ACTIVITY
            else -> CurrentScreenType.OTHERS
        }
    }


    fun isCurrentActivityBottomNavigationActivity(): Boolean {
        return currentScreenType == CurrentScreenType.BOTTOM_NAVIGATION_ACTIVITY
    }

    fun clear(){
        currentScreenType = null
    }
}
