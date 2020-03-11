package za.co.woolworths.financial.services.android.ui.activities.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.R
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.on_boarding_activity.*
import za.co.woolworths.financial.services.android.models.dto.OnBoardingModel
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.OnBoardingFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class OnBoardingActivity : AppCompatActivity(), IOnBoardingContract.View, View.OnClickListener {

    private var onBoardingViewModelImpl: OnBoardingViewModelImpl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.on_boarding_activity)
        KotlinUtils.setTransparentStatusBar(this)
        onBoardingViewModelImpl = OnBoardingViewModelImpl(this, OnBoardingModelImpl())

        AnimationUtilExtension.animateViewPushDown(signInButton)
        AnimationUtilExtension.animateViewPushDown(registerButton)
        AnimationUtilExtension.animateViewPushDown(letsGoButton)
        AnimationUtilExtension.animateViewPushDown(skipButton)

        skipButton?.setOnClickListener(this)
    }

    override fun showOnBoardingItems(onBoardingItems: MutableList<OnBoardingModel>) {
        with(onBoardingItems) {
            val onBoardingViewPagerAdapter = onBoardingViewPagerAdapter(this)
            with(onBoardingViewPager) {
                val onBoardingItemSize = onBoardingItems.size

                adapter = onBoardingViewPagerAdapter
                clipToPadding = false
                clipChildren = false
                offscreenPageLimit = onBoardingItems.size

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        when (position) {
                            (onBoardingItemSize - 1) -> {
                                skipButton?.visibility = GONE
                                letsGoButton?.visibility = VISIBLE
                            }
                            else -> {
                                skipButton?.visibility = VISIBLE
                                letsGoButton?.visibility = GONE
                            }
                        }
                    }
                })
            }
            configurePageIndicator(this)
        }
    }

    override fun onBoardingViewPagerAdapter(onBoardingItems: MutableList<OnBoardingModel>): FragmentStateAdapter {
        return object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment = OnBoardingFragment.newInstance(onBoardingItems[position])
            override fun getItemCount(): Int = onBoardingItems.size
        }
    }

    override fun configurePageIndicator(onBoardingItems: MutableList<OnBoardingModel>) {
        viewPagerIndicatorTabLayout?.let { tabLayout ->
            onBoardingViewPager?.let { viewPager ->
                TabLayoutMediator(tabLayout, viewPager) { tab, _ -> tab.text = "" }.attach()
            }
        }
    }

    override fun navigateToOnBoardingScreen() {
        startActivityForResult(Intent(this, BottomNavigationActivity::class.java), 0)
        finish()
        overridePendingTransition(R.anim.stay, R.anim.fade_out)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.skipButton, R.id.letsGoButton -> onBoardingViewModelImpl?.navigateToMain()
        }
    }
}