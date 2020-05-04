package za.co.woolworths.financial.services.android.ui.fragments.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.R
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.on_boarding_fragment.onBoardingViewPager
import kotlinx.android.synthetic.main.on_boarding_fragment.viewPagerIndicatorTabLayout
import za.co.woolworths.financial.services.android.contracts.IViewPagerSwipeListener
import za.co.woolworths.financial.services.android.models.dto.OnBoardingModel
import za.co.woolworths.financial.services.android.ui.activities.onboarding.IOnBoardingContract
import za.co.woolworths.financial.services.android.ui.activities.onboarding.OnBoardingModelImpl
import za.co.woolworths.financial.services.android.ui.activities.onboarding.OnBoardingViewModelImpl
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType

class OnBoardingFragment : Fragment(), IOnBoardingContract.View {

    private var mOnBoardingViewModelImpl: OnBoardingViewModelImpl? = null
    private var mViewPagerSwipeListener: IViewPagerSwipeListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is IViewPagerSwipeListener)
            mViewPagerSwipeListener = context as? IViewPagerSwipeListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.on_boarding_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mOnBoardingViewModelImpl = OnBoardingViewModelImpl(this, OnBoardingModelImpl())
        arguments?.let { args -> mOnBoardingViewModelImpl?.showOnBoardingView(args) }
    }

    override fun showOnBoardingItems(onBoardingItems: MutableList<OnBoardingModel>) {
        with(onBoardingItems) {
            with(onBoardingViewPager) {
                val onBoardingItemSize = onBoardingItems.size

                adapter = object : FragmentStateAdapter(this@OnBoardingFragment) {
                    override fun createFragment(position: Int): Fragment = OnBoardingContentFragment.newInstance(Pair(mOnBoardingViewModelImpl?.getOnBoardingScreenType() ?: OnBoardingScreenType.START_UP, onBoardingItems[position]))
                    override fun getItemCount(): Int = onBoardingItems.size
                }

                clipToPadding = false
                clipChildren = false
                offscreenPageLimit = onBoardingItems.size

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        mViewPagerSwipeListener?.onPagerSwipe(position, onBoardingItemSize)
                    }
                })
            }
            configurePageIndicator(this)
        }
    }

    override fun configurePageIndicator(onBoardingItems: MutableList<OnBoardingModel>) {
        viewPagerIndicatorTabLayout?.let { tabLayout ->
            onBoardingViewPager?.let { viewPager ->
                TabLayoutMediator(tabLayout, viewPager) { tab, _ -> tab.text = "" }.attach()
            }
        }
    }

    companion object {
        const val ON_BOARDING_SCREEN_TYPE = "ON_BOARDING_SCREEN_TYPE"
    }
}