package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.*
import za.co.woolworths.financial.services.android.ui.fragments.wreward.logged_in.WRewardsLoggedinAndLinkedFragment
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class WRewardsFragment : Fragment(R.layout.wrewards_fragment) {

    private var bottomNav: BottomNavigationActivity? = (activity as? BottomNavigationActivity)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init() {
        removeAllChildFragments()
        val isUserAuthenticated = SessionUtilities.getInstance().isUserAuthenticated
        val isC2User = SessionUtilities.getInstance().isC2User
        if (isUserAuthenticated && isC2User) {
            //user is linked and signed in
            linkSignIn()
        } else if (isUserAuthenticated && !isC2User) {
            //user is not linked
            //but signed in
            notLinkSignIn()
        } else if (!isUserAuthenticated) {
            //user is signed out
            signOut()
        }
    }

    private fun removeAllChildFragments() {
        activity?.supportFragmentManager?.apply {
            if (backStackEntryCount > 0) {
                for (fragment in fragments) {
                    beginTransaction()
                            .remove(fragment)
                            .commitAllowingStateLoss()
                }
            }
        }
    }

    private fun linkSignIn() {
        bottomNav?.hideToolbar()
        childFragmentManager.apply {
            with(beginTransaction()) {
                add(R.id.content_frame, WRewardsLoggedinAndLinkedFragment())
                commitAllowingStateLoss()
            }
        }
    }

    private fun notLinkSignIn() {
        bottomNav?.hideToolbar()
        childFragmentManager.apply {
            with(beginTransaction()) {
                add(R.id.content_frame, WRewardsLoggedInAndNotLinkedFragment())
                commitAllowingStateLoss()
            }
        }
    }

    private fun signOut() {
        bottomNav?.hideToolbar()
        childFragmentManager.apply {
            with(beginTransaction()) {
                add(R.id.content_frame, WRewardsLoggedOutFragment())
                commitAllowingStateLoss()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WRewardsVouchersFragment.LOCK_REQUEST_CODE_WREWARDS) {
            return
        }
        if (resultCode == SSOActivity.SSOActivityResult.SIGNED_OUT.rawValue()) {
            if (data != null) {
                val stsParams: String = data.getStringExtra("stsParams") ?: ""
                onSessionExpired(activity, stsParams)
            }
        } else if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            //One time biometricsWalkthrough
            ScreenManager.presentBiometricWalkthrough(activity)
        }
        init()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            bottomNav?.hideToolbar()
            init()
        }

        bottomNav?.setTitle(getString(R.string.wrewards))
    }

    private fun onSessionExpired(activity: Activity?, stsParams: String) {
        bottomNav = (activity as? BottomNavigationActivity)
        if (activity == null || bottomNav == null) return
        bottomNav?.apply {
            addBadge(INDEX_REWARD, 0)
            addBadge(INDEX_ACCOUNT, 0)
            addBadge(INDEX_CART, 0)
            // R.id.navigate_to_wreward) prevent session dialog expired popup
            // to appear when switching tab on an ongoing voucher call.
            // R.id.navigate_to_cart enable expired popup display when CartActivity is finished.
            Utils.setBadgeCounter(0)
            if (SessionUtilities.getInstance().isUserAuthenticated && (currentSection == R.id.navigate_to_wreward || currentSection == R.id.navigate_to_cart)) {
                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, stsParams)
                (activity as? AppCompatActivity)?.let { activity -> SessionExpiredUtilities.getInstance().showSessionExpireDialog(activity) }
            }
        }
        init()
    }

    fun getWRewardContentFrame(): Fragment? {
        return childFragmentManager.findFragmentById(R.id.content_frame)
    }
}