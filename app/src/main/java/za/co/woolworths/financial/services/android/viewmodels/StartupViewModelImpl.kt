package za.co.woolworths.financial.services.android.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

import com.google.firebase.analytics.FirebaseAnalytics

import java.util.ArrayList

import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.AbsaBankingOpenApiServices
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.models.dto.chat.PresenceInAppChat
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils

class StartupViewModelImpl(private val mContext: Context) : StartupViewModel {
    override var intent: Intent? = null
    override var pushNotificationUpdate: String? = null

    override var firebaseAnalytics: FirebaseAnalytics? = null

    override var appVersion: String? = null
    override var environment: String? = null

    override var videoPlayerShouldPlay = true
    override var isVideoPlaying = false
    override var isAppMinimized = false

    override var isServerMessageShown = false
    override var isSplashScreenDisplay = false
    override var isSplashScreenPersist = false
    override var splashScreenText = ""

    override val randomVideoPath: String
        get() {
            val listOfVideo = ArrayList<String>()
            val rawFolderPath = "android.resource://" + mContext.packageName + "/"
           // listOfVideo.add(rawFolderPath + R.raw.food_broccoli)
            listOfVideo.shuffle()
            return listOfVideo[0]
        }

    override fun queryServiceGetConfig(responseListener: IResponseListener<ConfigResponse?>) {

        val configResponseCall = OneAppService.getConfig()
        configResponseCall.enqueue(CompletionHandler(object : IResponseListener<ConfigResponse> {

            override fun onSuccess(response: ConfigResponse?) {
                if (response?.httpCode == 200) {
                    persistGlobalConfig(response)
                    responseListener.onSuccess(response)
                }
            }

            override fun onFailure(error: Throwable?) {
                responseListener.onFailure(error)

            }
        }, ConfigResponse::class.java))

    }

    override fun presentNextScreen() {

        val isFirstTime = Utils.getSessionDaoValue(SessionDao.KEY.ON_BOARDING_SCREEN)
        val appLinkData = intent?.data

        if (Intent.ACTION_VIEW == intent?.action && appLinkData != null) {
            handleAppLink(appLinkData)

        } else {
            val activity = mContext as Activity
            if (isFirstTime == null || Utils.isAppUpdated(mContext))
                ScreenManager.presentOnboarding(activity)
            else {
                ScreenManager.presentMain(activity, pushNotificationUpdate)
            }
        }
    }

    private fun handleAppLink(appLinkData: Uri) {
        val productSearchViewModel: ProductSearchViewModel = ProductSearchViewModelImpl();
        //productSearchViewModel.getTypeAndTerm(urlString = appLinkData.toString())
        //1. check URL
        //2. navigate to facet that URL corresponds to
        ScreenManager.presentMain(mContext as Activity, pushNotificationUpdate, appLinkData)
    }

    private fun persistGlobalConfig(response: ConfigResponse) {
        splashScreenText = response.configs.enviroment.splashScreenText
        isSplashScreenDisplay = response.configs.enviroment.splashScreenDisplay
        isSplashScreenPersist = response.configs.enviroment.splashScreenPersist

        WoolworthsApplication.setStoreCardBlockReasons(response.configs.enviroment.storeCardBlockReasons)
        WoolworthsApplication.setSsoRedirectURI(response.configs.enviroment.getSsoRedirectURI())
        WoolworthsApplication.setStsURI(response.configs.enviroment.getStsURI())
        WoolworthsApplication.setSsoRedirectURILogout(response.configs.enviroment.getSsoRedirectURILogout())
        WoolworthsApplication.setSsoUpdateDetailsRedirectUri(response.configs.enviroment.getSsoUpdateDetailsRedirectUri())
        WoolworthsApplication.setWwTodayURI(response.configs.enviroment.getWwTodayURI())
        WoolworthsApplication.setAuthenticVersionReleaseNote(response.configs.enviroment.getAuthenticVersionReleaseNote())
        WoolworthsApplication.setAuthenticVersionStamp(response.configs.enviroment.getAuthenticVersionStamp())
        WoolworthsApplication.setRegistrationTCLink(response.configs.defaults.registerTCLink)
        WoolworthsApplication.setFaqLink(response.configs.defaults.faqLink)
        WoolworthsApplication.setWrewardsLink(response.configs.defaults.wrewardsLink)
        WoolworthsApplication.setRewardingLink(response.configs.defaults.rewardingLink)
        WoolworthsApplication.setHowToSaveLink(response.configs.defaults.howtosaveLink)
        WoolworthsApplication.setWrewardsTCLink(response.configs.defaults.wrewardsTCLink)
        WoolworthsApplication.setCartCheckoutLink(response.configs.defaults.cartCheckoutLink)
        WoolworthsApplication.setQuickShopDefaultValues(response.configs.quickShopDefaultValues)
        WoolworthsApplication.setWhitelistedDomainsForQRScanner(response.configs.whitelistedDomainsForQRScanner)
        WoolworthsApplication.setStsValues(response.configs.sts)

        WoolworthsApplication.setApplyNowLink(response.configs.applyNowLinks)

        var absaBankingOpenApiServices: AbsaBankingOpenApiServices? = response.configs.absaBankingOpenApiServices
        if (absaBankingOpenApiServices == null) {
            absaBankingOpenApiServices = AbsaBankingOpenApiServices(false, "", "", "", "")
        } else {
            absaBankingOpenApiServices.isEnabled = Utils.isFeatureEnabled(absaBankingOpenApiServices.minimumSupportedAppBuildNumber)
        }
        var presenceInAppChat: PresenceInAppChat? = response.configs.presenceInAppChat
        if (presenceInAppChat == null) {
            presenceInAppChat = PresenceInAppChat(ArrayList(), "", false)
        } else {
            presenceInAppChat.isEnabled = Utils.isFeatureEnabled(presenceInAppChat.minimumSupportedAppBuildNumber)
        }

        val instantCardReplacement = response.configs.instantCardReplacement
        if (instantCardReplacement != null) {
            instantCardReplacement.isEnabled = Utils.isFeatureEnabled(instantCardReplacement.minimumSupportedAppBuildNumber)
        }

        val virtualTempCard = response.configs.virtualTempCard
        if (virtualTempCard != null) {
            virtualTempCard.isEnabled = Utils.isFeatureEnabled(virtualTempCard.minimumSupportedAppBuildNumber)
        }

        WoolworthsApplication.setAbsaBankingOpenApiServices(absaBankingOpenApiServices)
        WoolworthsApplication.setPresenceInAppChat(presenceInAppChat)

        WoolworthsApplication.setInstantCardReplacement(instantCardReplacement)
        WoolworthsApplication.setVirtualTempCard(virtualTempCard)

        WoolworthsApplication.getInstance().wGlobalState.startRadius = response.configs.enviroment.getStoreStockLocatorConfigStartRadius()
        WoolworthsApplication.getInstance().wGlobalState.endRadius = response.configs.enviroment.getStoreStockLocatorConfigEndRadius()
        val creditCardActivation = response.configs.creditCardActivation
        creditCardActivation?.apply {
            isEnabled = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
        }
        WoolworthsApplication.setCreditCardActivation(creditCardActivation)
    }

    companion object {

        val APP_SERVER_ENVIRONMENT_KEY = "app_server_environment"
        val APP_VERSION_KEY = "app_version"
    }
}
