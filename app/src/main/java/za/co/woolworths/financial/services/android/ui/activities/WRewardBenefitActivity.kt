package za.co.woolworths.financial.services.android.ui.activities

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.awfs.coordination.R
import com.awfs.coordination.databinding.RewardBenefitActivityBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import za.co.woolworths.financial.services.android.ui.fragments.wreward.RewardBenefitFragment
import za.co.woolworths.financial.services.android.ui.fragments.wreward.VIPExclusiveFragment
import za.co.woolworths.financial.services.android.ui.fragments.wreward.unique_locators.WRewardsUniqueLocators
import za.co.woolworths.financial.services.android.util.Utils


class WRewardBenefitActivity : AppCompatActivity() {

    private lateinit var binding: RewardBenefitActivityBinding
    private var benefitTabPosition: Int = 0

    companion object {
        fun convertWRewardCharacter(description: String): SpannableStringBuilder {
            val spanBuilder = SpannableStringBuilder(description)
            with(spanBuilder) {
                if (contains("WRe")) {
                    val rewardTextPosition = indexOf("WRewards")
                    val updateWCharacterPosition = rewardTextPosition + 1
                    setSpan(StyleSpan(Typeface.BOLD), rewardTextPosition, updateWCharacterPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(ForegroundColorSpan(Color.GRAY), rewardTextPosition, updateWCharacterPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            return spanBuilder
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = RewardBenefitActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)
        benefitTabPosition = intent.getIntExtra("benefitTabPosition", 0)
        binding.init()
        binding.closeIcon.apply {
            setOnClickListener { onBackPressed() }
            contentDescription = WRewardsUniqueLocators.CLOSE.value
        }
        binding.header.contentDescription = WRewardsUniqueLocators.WREWARDS_BENEFITS_IMAGE.value
    }

    private fun RewardBenefitActivityBinding.init() {
        vpRewardBenefit?.adapter = object : FragmentStateAdapter(this@WRewardBenefitActivity) {
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> RewardBenefitFragment.newInstance()
                    else -> VIPExclusiveFragment.newInstance()
                }
            }

            override fun getItemCount(): Int {
                return 2
            }
        }

        TabLayoutMediator(tabs, vpRewardBenefit) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.benefits)
                else -> getString(R.string.vip_exclusive)
            }
        }.attach()

        vpRewardBenefit?.currentItem = benefitTabPosition

        updateTabFont(benefitTabPosition, true)

        tabs?.addOnTabSelectedListener(
                object : TabLayout.OnTabSelectedListener {
                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                        updateTabFont(tab?.position ?: 0, false)
                    }

                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        updateTabFont(tab?.position ?: 0, true)
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }
                })
    }

    private fun RewardBenefitActivityBinding.updateTabFont(position: Int, tabIsSelected: Boolean) {
        val tabLayout =
                (tabs.getChildAt(0) as? ViewGroup)?.getChildAt(position) as? LinearLayout
        val tabTextView = tabLayout?.getChildAt(1) as? AppCompatTextView
        val typeface =
                ResourcesCompat.getFont(this@WRewardBenefitActivity, if (tabIsSelected) R.font.futura_semi_bold_ttf else R.font.futura_medium_ttf)
        tabTextView?.setTypeface(typeface, Typeface.NORMAL)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }
}