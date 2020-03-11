package za.co.woolworths.financial.services.android.ui.activities.onboarding

import androidx.viewpager2.adapter.FragmentStateAdapter
import za.co.woolworths.financial.services.android.models.dto.OnBoardingModel

interface IOnBoardingContract {
    interface View {
        fun showOnBoardingItems(onBoardingItems: MutableList<OnBoardingModel>)
        fun onBoardingViewPagerAdapter(onBoardingItems: MutableList<OnBoardingModel>): FragmentStateAdapter
        fun  configurePageIndicator(onBoardingItems: MutableList<OnBoardingModel>)
        fun navigateToOnBoardingScreen()
    }

    interface Presenter {
        fun setupForOneTimeVideoOnSplashScreen()
        fun saveApplicationVersion()
        fun navigateToMain()
    }

    interface ModelInteractor {
        fun getOnBoardingModel(): MutableList<OnBoardingModel>
    }
}