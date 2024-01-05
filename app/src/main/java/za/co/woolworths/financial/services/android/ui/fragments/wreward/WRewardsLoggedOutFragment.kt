package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.WrewardLogoutFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.fragments.wreward.unique_locators.WRewardUniqueLocatorsHelper
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils

class WRewardsLoggedOutFragment : WRewardOnBoardingFragment(R.layout.wreward_logout_fragment), View.OnClickListener {

    private lateinit var binding: WrewardLogoutFragmentBinding
    private var mBottomNavigator: BottomNavigator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBottomNavigator = activity as? BottomNavigator?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = WrewardLogoutFragmentBinding.bind(view)

        binding.apply {
            mBottomNavigator?.removeToolbar()

            underlineText(incSignOutContent.tvSignIn)
            underlineText(incSignOutContent.tvRegister)

            setUpPager(vpJoinRewardInfo, tabIndicator)

            incSignOutContent.applyForWRewards.setOnClickListener(this@WRewardsLoggedOutFragment)
            incSignOutContent.tvSignIn.setOnClickListener(this@WRewardsLoggedOutFragment)
            incSignOutContent.tvRegister.setOnClickListener(this@WRewardsLoggedOutFragment)
            WRewardUniqueLocatorsHelper.setRewardsSignedOutMainIDs(incSignOutContent.applyForWRewards,incSignOutContent.tvSignIn,incSignOutContent.tvOr,incSignOutContent.tvRegister)
        }
    }

    override fun onClick(view: View?) {
        activity?.apply {
            when (view?.id) {
                R.id.applyForWRewards -> Utils.openLinkInInternalWebView( AppConfigSingleton.wrewardsLink, true)
                R.id.tvSignIn -> {
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSSIGNIN, this)
                    ScreenManager.presentSSOSignin(this)
                }
                R.id.tvRegister -> {
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSREGISTER, this)
                    ScreenManager.presentSSORegister(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let { activity -> Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_SIGNED_OUT) }
    }

    fun scrollToTop() = binding.scrollLoggedOutLoggedIn?.let { view -> ObjectAnimator.ofInt(view, "scrollY", view.scrollY, 0).setDuration(500).start() }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        mBottomNavigator?.removeToolbar()
    }
}
