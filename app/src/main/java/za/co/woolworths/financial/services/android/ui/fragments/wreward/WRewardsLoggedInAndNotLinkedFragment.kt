package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.WrewardsLoggedoutLoggedinNotlinkedBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Utils.setScreenName

class WRewardsLoggedInAndNotLinkedFragment : WRewardOnBoardingFragment(R.layout.wrewards_loggedout_loggedin_notlinked) {

    private lateinit var binding: WrewardsLoggedoutLoggedinNotlinkedBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = WrewardsLoggedoutLoggedinNotlinkedBinding.bind(view)

        binding.apply {
            hideToolbar()
            underlineText(tvRewardLinkAccount)
            setUpPager(vpJoinRewardInfo, tabIndicator)
            applyForWRewards?.setOnClickListener {
                Utils.openLinkInInternalWebView(
                    AppConfigSingleton.wrewardsLink,
                    true
                )
            }
            tvRewardLinkAccount?.setOnClickListener {
                activity?.let { activity ->
                    ScreenManager.presentSSOLinkAccounts(
                        activity
                    )
                };
            }
            uniqueIdsForWRewardsLoggedInNotLinkAutomation()
        }
    }

    private fun WrewardsLoggedoutLoggedinNotlinkedBinding.uniqueIdsForWRewardsLoggedInNotLinkAutomation() {
        activity?.resources?.apply {
            vpJoinRewardInfo?.contentDescription = getString(R.string.join_wreward_view_pager)
            scrollLoggedOutLoggedIn?.contentDescription = getString(R.string.logged_in_Not_link_scrollView)
            applyForWRewards?.contentDescription = getString(R.string.apply_for_wreward_layout)
            loggedInNotLinkedLinearLayout?.contentDescription = getString(R.string.logged_in_not_linked_linearlayout)
        }
    }

    override fun onResume() {
        super.onResume()
        if(isVisible) {
            activity?.let { activity ->
                setScreenName(
                    activity,
                    FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_SIGNED_IN_NOT_LINKED
                )
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            hideToolbar()
    }

    private fun hideToolbar() = (activity as? BottomNavigationActivity)?.removeToolbar()

    fun scrollToTop() = ObjectAnimator.ofInt(binding.scrollLoggedOutLoggedIn, "scrollY", binding.scrollLoggedOutLoggedIn.scrollY, 0)?.setDuration(500)?.start()
}