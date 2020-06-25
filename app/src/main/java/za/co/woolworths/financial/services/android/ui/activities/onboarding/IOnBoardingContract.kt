package za.co.woolworths.financial.services.android.ui.activities.onboarding

import za.co.woolworths.financial.services.android.models.dto.OnBoardingModel
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType

interface IOnBoardingContract {
    interface View {
        fun showOnBoardingItems(onBoardingItems: MutableList<OnBoardingModel>) {}
        fun configurePageIndicator(onBoardingItems: MutableList<OnBoardingModel>) {}
        fun navigateToBottomNavigationActivity() {}
    }

    interface Presenter {
        fun setupForOneTimeVideoOnSplashScreen()
        fun saveApplicationVersion()
        fun navigateToMain()
        fun getOnBoardingScreenType(): OnBoardingScreenType
    }

    interface ModelInteractor {
        fun getStartupOnBoardingModel(): MutableList<OnBoardingModel>
        fun getAccountOnBoardingModel(): MutableList<OnBoardingModel>
    }
}