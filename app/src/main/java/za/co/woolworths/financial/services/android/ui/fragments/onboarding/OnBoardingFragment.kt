package za.co.woolworths.financial.services.android.ui.fragments.onboarding

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.databinding.OnBoardingFragmentBinding
import com.google.android.material.tabs.TabLayoutMediator
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IViewPagerSwipeListener
import za.co.woolworths.financial.services.android.models.dto.OnBoardingModel
import za.co.woolworths.financial.services.android.ui.activities.onboarding.IOnBoardingContract
import za.co.woolworths.financial.services.android.ui.activities.onboarding.OnBoardingModelImpl
import za.co.woolworths.financial.services.android.ui.activities.onboarding.OnBoardingViewModelImpl
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType

class OnBoardingFragment : BaseFragmentBinding<OnBoardingFragmentBinding>(OnBoardingFragmentBinding::inflate), IOnBoardingContract.View {

    private var mOnBoardingViewModelImpl: OnBoardingViewModelImpl? = null
    private var mViewPagerSwipeListener: IViewPagerSwipeListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is IViewPagerSwipeListener)
            mViewPagerSwipeListener = context as? IViewPagerSwipeListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mOnBoardingViewModelImpl = OnBoardingViewModelImpl(this, OnBoardingModelImpl())
        arguments?.let { args -> mOnBoardingViewModelImpl?.showOnBoardingView(args) }
    }

    override fun showOnBoardingItems(onBoardingItems: MutableList<OnBoardingModel>) {
        with(onBoardingItems) {
            with(binding.onBoardingViewPager) {
                val onBoardingItemSize = onBoardingItems.size

                adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
                    override fun createFragment(position: Int): Fragment = OnBoardingContentFragment.newInstance(Pair(mOnBoardingViewModelImpl?.getOnBoardingScreenType() ?: OnBoardingScreenType.START_UP, onBoardingItems[position]))
                    override fun getItemCount(): Int = onBoardingItems.size
                }

                clipToPadding = false
                clipChildren = false
                offscreenPageLimit = onBoardingItems.size

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        fireBaseScreensNames(position)
                        mViewPagerSwipeListener?.onPagerSwipe(position, onBoardingItemSize)
                    }
                })
            }
            configurePageIndicator(this)
        }
    }

    override fun configurePageIndicator(onBoardingItems: MutableList<OnBoardingModel>) {
        binding.viewPagerIndicatorTabLayout?.let { tabLayout ->
            binding.onBoardingViewPager?.let { viewPager ->
                TabLayoutMediator(tabLayout, viewPager) { tab, _ -> tab.text = "" }.attach()
            }
        }
    }
    fun fireBaseScreensNames(position: Int){
        var screenName = when(position){
            0 -> FirebaseManagerAnalyticsProperties.ScreenNames.ONBOARDING_ONE
            1 -> FirebaseManagerAnalyticsProperties.ScreenNames.ONBOARDING_TWO
            2 -> FirebaseManagerAnalyticsProperties.ScreenNames.ONBOARDING_THREE
            3 -> FirebaseManagerAnalyticsProperties.ScreenNames.ONBOARDING_FOUR
            else -> ""
        }
        Utils.setScreenName(screenName)
    }
    companion object {
        const val ON_BOARDING_SCREEN_TYPE = "ON_BOARDING_SCREEN_TYPE"
    }
}