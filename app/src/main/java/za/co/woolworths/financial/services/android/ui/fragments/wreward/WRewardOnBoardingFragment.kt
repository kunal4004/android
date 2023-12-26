package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.graphics.Paint
import android.view.animation.Animation
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import za.co.woolworths.financial.services.android.ui.adapters.JoinRewardAdapter
import za.co.woolworths.financial.services.android.ui.fragments.wreward.unique_locators.WRewardUniqueLocatorsHelper

abstract class WRewardOnBoardingFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    fun setUpPager(vpJoinRewardInfo: ViewPager2, tabIndicator: TabLayout) {
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

        TabLayoutMediator(tabIndicator, vpJoinRewardInfo) {  tab, index ->
            tab.text = ""
            WRewardUniqueLocatorsHelper.setIndicatorsLocators(tab,index)
        }.attach()
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val a = object : Animation() {
        }
        a.duration = 0
        return a
    }

    fun underlineText(tvText: TextView?) {
        tvText?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    }
}