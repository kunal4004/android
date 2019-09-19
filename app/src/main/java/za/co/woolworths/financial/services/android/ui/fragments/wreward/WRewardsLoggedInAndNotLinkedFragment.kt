package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.wrewards_loggedout_loggedin_notlinked.*
import kotlinx.android.synthetic.main.wrewards_loggedout_loggedin_notlinked.scrollLoggedOutLoggedIn
import kotlinx.android.synthetic.main.wrewards_loggedout_loggedin_notlinked.tabIndicator
import kotlinx.android.synthetic.main.wrewards_loggedout_loggedin_notlinked.vpJoinRewardInfo
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Utils.setScreenName

class WRewardsLoggedInAndNotLinkedFragment : WRewardOnBoardingFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.wrewards_loggedout_loggedin_notlinked, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideToolbar()
        underlineText(tvRewardLinkAccount)
        setUpPager(vpJoinRewardInfo, tabIndicator)
        applyForWRewards?.setOnClickListener { activity?.let { activity -> Utils.openExternalLink(activity, WoolworthsApplication.getWrewardsLink()) } }
        tvRewardLinkAccount?.setOnClickListener { activity?.let { activity -> ScreenManager.presentSSOLinkAccounts(activity) }; }
    }

    override fun onResume() {
        super.onResume()
        activity?.let { activity -> setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_SIGNED_IN_NOT_LINKED) }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            hideToolbar()
    }

    private fun hideToolbar() = (activity as? BottomNavigationActivity)?.removeToolbar()
    fun scrollToTop() = ObjectAnimator.ofInt(scrollLoggedOutLoggedIn, "scrollY", scrollLoggedOutLoggedIn.scrollY, 0)?.setDuration(500)?.start()

}