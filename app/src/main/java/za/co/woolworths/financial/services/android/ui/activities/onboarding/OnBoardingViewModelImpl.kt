package za.co.woolworths.financial.services.android.ui.activities.onboarding

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import com.crashlytics.android.Crashlytics
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.OnBoardingModel
import za.co.woolworths.financial.services.android.util.Utils

class OnBoardingViewModelImpl(private var onBoardingView: IOnBoardingContract.View?, private var onBoardingModel: IOnBoardingContract.ModelInteractor) : ViewModel(), IOnBoardingContract.Presenter, IOnBoardingContract.ModelInteractor {

    init {
        onBoardingView?.showOnBoardingItems(getOnBoardingModel())
        setupForOneTimeVideoOnSplashScreen()
        saveApplicationVersion()
    }

    override fun getOnBoardingModel(): MutableList<OnBoardingModel> {
        return onBoardingModel.getOnBoardingModel()
    }

    override fun setupForOneTimeVideoOnSplashScreen() {
        try {
            Utils.sessionDaoSave(SessionDao.KEY.SPLASH_VIDEO, "1")
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }
    }

    override fun saveApplicationVersion() {
        val context = WoolworthsApplication.getAppContext()
        try {
            val appLatestVersion: String? = context?.packageManager?.getPackageInfo(context.packageName, 0)?.versionName
            Utils.sessionDaoSave(SessionDao.KEY.APP_VERSION, appLatestVersion)
        } catch (e: PackageManager.NameNotFoundException) {
            Crashlytics.logException(e)
        }
    }

    override fun navigateToMain() {
        try {
            Utils.sessionDaoSave(SessionDao.KEY.ON_BOARDING_SCREEN, "1")
            onBoardingView?.navigateToOnBoardingScreen()
        } catch (e: java.lang.Exception) {
            Crashlytics.logException(e)
        }
    }
}