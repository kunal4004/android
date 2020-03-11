package za.co.woolworths.financial.services.android.ui.activities.onboarding

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.OnBoardingModel

class OnBoardingModelImpl : IOnBoardingContract.ModelInteractor {

    override fun getOnBoardingModel(): MutableList<OnBoardingModel> {

        val onBoardingLists = mutableListOf<OnBoardingModel>()
        with(onBoardingLists) {
            add(OnBoardingModel(R.string.on_boarding_screen_title_1, R.drawable.walkthrough_welcome_cards))
            add(OnBoardingModel(R.string.on_boarding_screen_title_2, R.drawable.walkthrough_2))
            add(OnBoardingModel(R.string.on_boarding_screen_title_3, R.drawable.walkthrough_3))
            add(OnBoardingModel(R.string.on_boarding_screen_title_4, R.drawable.walkthrough_4))
        }

        return onBoardingLists
    }
}