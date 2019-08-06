package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.util.Utils
import android.animation.ObjectAnimator
import android.graphics.Color
import kotlinx.android.synthetic.main.wreward_logout_fragment.*
import kotlinx.android.synthetic.main.wreward_sign_out_content.*
import android.graphics.Paint.UNDERLINE_TEXT_FLAG
import android.view.*
import za.co.woolworths.financial.services.android.ui.adapters.JoinRewardAdapter
import com.google.android.material.tabs.TabLayoutMediator
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator

class WRewardsLoggedOutFragment : Fragment(), View.OnClickListener {

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

        tvSignIn?.paintFlags = UNDERLINE_TEXT_FLAG
        tvRegister?.paintFlags = UNDERLINE_TEXT_FLAG

        setUpPager()

        applyForWRewards.setOnClickListener(this)
        tvSignIn.setOnClickListener(this)
        tvRegister.setOnClickListener(this)
    }

    private fun setUpPager() {
        val joinRewardAdapter: JoinRewardAdapter? = JoinRewardAdapter()
        vpJoinRewardInfo.adapter = joinRewardAdapter
        val joinRewardBenefitList = mutableListOf<Triple<Int, Int, Int>>()

        with(joinRewardBenefitList) {
            add(Triple(R.drawable.join_reward_default_header, R.string.all_reason_to_join_title, R.string.all_reason_to_join_desc))
            add(Triple(R.drawable.join_reward_second_header, R.string.saving_woolies_favorite_every_day_title, R.string.saving_woolies_favorite_every_day_desc))
            add(Triple(R.drawable.join_reward_third_header, R.string.extra_saving_promo_event_title, R.string.extra_saving_promo_event_desc))
            add(Triple(R.drawable.join_reward_fourth_header, R.string.exclusive_voucher_just_for_you_title, R.string.exclusive_voucher_just_for_you_desc))
        }

        joinRewardAdapter?.setItem(joinRewardBenefitList)

        with(vpJoinRewardInfo) {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = joinRewardBenefitList.size
        }

        TabLayoutMediator(tabIndicator, vpJoinRewardInfo, TabLayoutMediator.OnConfigureTabCallback { tab, _ ->
            tab.text = ""
        }).attach()
    }

    override fun onClick(view: View?) {
        activity?.apply {
            when (view?.id) {
                R.id.applyForWRewards -> Utils.openExternalLink(this, WoolworthsApplication.getWrewardsLink())
                R.id.tvSignIn -> {
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSSIGNIN)
                    ScreenManager.presentSSOSignin(this)
                }
                R.id.tvRegister -> {
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSREGISTER)
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
}
