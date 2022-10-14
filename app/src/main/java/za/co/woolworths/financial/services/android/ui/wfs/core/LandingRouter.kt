package za.co.woolworths.financial.services.android.ui.wfs.core

import android.app.Activity
import androidx.fragment.app.Fragment
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import javax.inject.Inject

interface IRouter {
    fun push(fragment: Fragment, isPushFragmentUp : Boolean = false)
}

class LandingRouter @Inject constructor(private val activity: Activity) : IRouter {

    override fun push(fragment: Fragment, isPushedUp : Boolean) {
            when (activity) {
                is BottomNavigationActivity -> if (isPushedUp) activity.pushFragment(fragment) else activity.pushFragment(fragment)
                is MyAccountActivity -> activity.replaceFragment(fragment)
            }
    }

}