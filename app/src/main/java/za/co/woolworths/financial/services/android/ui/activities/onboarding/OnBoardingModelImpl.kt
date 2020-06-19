package za.co.woolworths.financial.services.android.ui.activities.onboarding

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.OnBoardingModel

class OnBoardingModelImpl : IOnBoardingContract.ModelInteractor {

    override fun getStartupOnBoardingModel(): MutableList<OnBoardingModel> {

        val onBoardingLists = mutableListOf<OnBoardingModel>()
        with(onBoardingLists) {
            add(OnBoardingModel(R.string.on_boarding_screen_title_1, R.drawable.walkthrough_welcome))
            add(OnBoardingModel(R.string.on_boarding_screen_title_2, R.drawable.walkthrough_shop))
            add(OnBoardingModel(R.string.on_boarding_screen_title_3, R.drawable.welcome_wrewards))
            add(OnBoardingModel(R.string.on_boarding_screen_title_4, R.drawable.walkthrough_wrewards))
        }

        return onBoardingLists
    }

    override fun getAccountOnBoardingModel(): MutableList<OnBoardingModel> {
        val onBoardingLists = mutableListOf<OnBoardingModel>()
        with(onBoardingLists) {
            add(OnBoardingModel(R.string.on_boarding_screen_title_1, R.drawable.accounts_walkthrough_1))
            add(OnBoardingModel(R.string.on_boarding_screen_title_2, R.drawable.accounts_walkthrough_2))
            add(OnBoardingModel(R.string.on_boarding_screen_title_3, R.drawable.accounts_walkthrough_3))
            add(OnBoardingModel(R.string.on_boarding_screen_title_4, R.drawable.accounts_walkthrough_4))
        }
        return onBoardingLists
    }
}