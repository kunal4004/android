package za.co.woolworths.financial.services.android.ui.activities.onboarding

import android.os.Bundle
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.OnBoardingModel
import za.co.woolworths.financial.services.android.ui.fragments.onboarding.OnBoardingFragment
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType

class OnBoardingViewModelImpl(private var onBoardingView: IOnBoardingContract.View?, private var onBoardingModel: IOnBoardingContract.ModelInteractor) : IOnBoardingContract.Presenter, IOnBoardingContract.ModelInteractor {

    private lateinit var onBoardingScreenType: OnBoardingScreenType

    fun showOnBoardingView(arguments: Bundle) {
        onBoardingScreenType = arguments.get(OnBoardingFragment.ON_BOARDING_SCREEN_TYPE) as OnBoardingScreenType

        onBoardingView?.showOnBoardingItems(when (onBoardingScreenType) {
            OnBoardingScreenType.START_UP -> getStartupOnBoardingModel()
            OnBoardingScreenType.ACCOUNT -> getAccountOnBoardingModel()
        })

        setupForOneTimeVideoOnSplashScreen()
        saveApplicationVersion()
    }

    override fun getStartupOnBoardingModel(): MutableList<OnBoardingModel> {
        return onBoardingModel.getStartupOnBoardingModel()
    }

    override fun getAccountOnBoardingModel(): MutableList<OnBoardingModel> {
        return onBoardingModel.getAccountOnBoardingModel()
    }

    override fun setupForOneTimeVideoOnSplashScreen() {
        Utils.sessionDaoSave(SessionDao.KEY.SPLASH_VIDEO, "1")
    }

    override fun saveApplicationVersion() {
        val context = WoolworthsApplication.getAppContext()
        val appLatestVersion: String? =
                context?.packageManager?.getPackageInfo(context.packageName, 0)?.versionName
        Utils.sessionDaoSave(SessionDao.KEY.APP_VERSION, appLatestVersion)
    }

    override fun navigateToMain() {
        Utils.sessionDaoSave(SessionDao.KEY.ON_BOARDING_SCREEN, "1")
        onBoardingView?.navigateToBottomNavigationActivity()
    }

    override fun getOnBoardingScreenType(): OnBoardingScreenType = onBoardingScreenType
}