package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.os.Bundle
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.util.Utils
import android.animation.ObjectAnimator
import kotlinx.android.synthetic.main.wreward_logout_fragment.*
import kotlinx.android.synthetic.main.wreward_sign_out_content.*
import android.view.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator

class WRewardsLoggedOutFragment : WRewardOnBoardingFragment(), View.OnClickListener {

    private var mBottomNavigator: BottomNavigator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBottomNavigator = activity as? BottomNavigator?
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.wreward_logout_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBottomNavigator?.removeToolbar()

        underlineText(tvSignIn)
        underlineText(tvRegister)

        setUpPager(vpJoinRewardInfo, tabIndicator)

        applyForWRewards.setOnClickListener(this)
        tvSignIn.setOnClickListener(this)
        tvRegister.setOnClickListener(this)

        uniqueIdsForWRewardAutomation()
    }

    override fun onClick(view: View?) {
        activity?.apply {
            when (view?.id) {
                R.id.applyForWRewards -> Utils.openLinkInInternalWebView( WoolworthsApplication.getWrewardsLink())
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

    fun scrollToTop() = scrollLoggedOutLoggedIn?.let { view -> ObjectAnimator.ofInt(view, "scrollY", view.scrollY, 0).setDuration(500).start() }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        mBottomNavigator?.removeToolbar()
    }

    private fun uniqueIdsForWRewardAutomation() {
        activity?.resources?.apply {
            vpJoinRewardInfo?.contentDescription = getString(R.string.joinWRewardsViewGroup)
            scrollLoggedOutLoggedIn?.contentDescription = getString(R.string.join_wreward_nested_scrollview)
            joinRewardScrollContainerLinearLayout?.contentDescription = getString(R.string.join_wreward_scroll_container_linearlayout)
            tabIndicator?.contentDescription = getString(R.string.join_wreward_tab_indicator_layout)
            incSignOutContent?.contentDescription = getString(R.string.include_sign_out_content)
            applyForWRewards?.contentDescription = getString(R.string.joinWRewardsButton)
        }
    }
}
