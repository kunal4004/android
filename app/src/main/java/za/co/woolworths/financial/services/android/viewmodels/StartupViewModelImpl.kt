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
import za.co.woolworths.financial.services.android.models.dto.chat.Collections
import za.co.woolworths.financial.services.android.models.dto.chat.CustomerService
import za.co.woolworths.financial.services.android.models.dto.chat.PresenceInAppChat
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils

class StartupViewModelImpl(private val mContext: Context) : StartupViewModel {
    override var intent: Intent? = null
//    override var pushNotificationUpdate: String? = null

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
            // val rawFolderPath = "android.resource://" + mContext.packageName + "/"
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
                ScreenManager.presentMain(activity)
            }
        }
    }

    private fun handleAppLink(appLinkData: Uri) {
        // val productSearchViewModel: ProductSearchViewModel = ProductSearchViewModelImpl();
        //productSearchViewModel.getTypeAndTerm(urlString = appLinkData.toString())
        //1. check URL
        //2. navigate to facet that URL corresponds to
        ScreenManager.presentMain(mContext as Activity, appLinkData)
    }

    private fun persistGlobalConfig(response: ConfigResponse) {
        response.configs?.apply {

            enviroment?.apply {
                this@StartupViewModelImpl.splashScreenText = splashScreenText
                this@StartupViewModelImpl.isSplashScreenDisplay = splashScreenDisplay
                this@StartupViewModelImpl.isSplashScreenPersist = splashScreenPersist

                WoolworthsApplication.setStoreCardBlockReasons(storeCardBlockReasons)
                WoolworthsApplication.setSsoRedirectURI(getSsoRedirectURI())
                WoolworthsApplication.setStsURI(getStsURI())
                WoolworthsApplication.setSsoRedirectURILogout(getSsoRedirectURILogout())
                WoolworthsApplication.setSsoUpdateDetailsRedirectUri(getSsoUpdateDetailsRedirectUri())
                WoolworthsApplication.setWwTodayURI(getWwTodayURI())
                WoolworthsApplication.setAuthenticVersionReleaseNote(getAuthenticVersionReleaseNote())
                WoolworthsApplication.setAuthenticVersionStamp(getAuthenticVersionStamp())
                WoolworthsApplication.getInstance().wGlobalState.startRadius =
                        getStoreStockLocatorConfigStartRadius()
                WoolworthsApplication.getInstance().wGlobalState.endRadius =
                        getStoreStockLocatorConfigEndRadius()
            }


            defaults?.apply {
                WoolworthsApplication.setRegistrationTCLink(registerTCLink)
                WoolworthsApplication.setFaqLink(faqLink)
                WoolworthsApplication.setWrewardsLink(wrewardsLink)
                WoolworthsApplication.setRewardingLink(rewardingLink)
                WoolworthsApplication.setHowToSaveLink(howtosaveLink)
                WoolworthsApplication.setWrewardsTCLink(wrewardsTCLink)
                WoolworthsApplication.setCartCheckoutLink(cartCheckoutLink)
            }

            whatsApp?.apply {
                showWhatsAppButton = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
                WoolworthsApplication.setWhatsAppConfig(this)
            }

            WoolworthsApplication.setQuickShopDefaultValues(quickShopDefaultValues)
            WoolworthsApplication.setWhitelistedDomainsForQRScanner(whitelistedDomainsForQRScanner)
            WoolworthsApplication.setStsValues(sts)
            WoolworthsApplication.setApplyNowLink(applyNowLinks)

            var absaBankingOpenApiServices: AbsaBankingOpenApiServices? = absaBankingOpenApiServices
            if (absaBankingOpenApiServices == null) {
                absaBankingOpenApiServices = AbsaBankingOpenApiServices(false, "", "", "", "")
            } else {
                absaBankingOpenApiServices.isEnabled =
                        Utils.isFeatureEnabled(absaBankingOpenApiServices.minimumSupportedAppBuildNumber)
            }


            var presenceInAppChat: PresenceInAppChat? = presenceInAppChat
            if (presenceInAppChat == null) {
                presenceInAppChat = PresenceInAppChat(ArrayList(), "", null, false, Collections("", "", "", "", mutableListOf()), CustomerService("", "", "", "", mutableListOf()))
            } else {
                presenceInAppChat.isEnabled =
                        Utils.isFeatureEnabled(presenceInAppChat.minimumSupportedAppBuildNumber)
            }

            val virtualTempCard = virtualTempCard
            if (virtualTempCard != null) {
                virtualTempCard.isEnabled = Utils.isFeatureEnabled(virtualTempCard.minimumSupportedAppBuildNumber)
            }

            WoolworthsApplication.setContactUsDetails(contactUs)

            WoolworthsApplication.setInAppChat(inAppChat)

            WoolworthsApplication.setAbsaBankingOpenApiServices(absaBankingOpenApiServices)
            WoolworthsApplication.setPresenceInAppChat(presenceInAppChat)

            instantCardReplacement?.isEnabled = instantCardReplacement?.minimumSupportedAppBuildNumber?.let { Utils.isFeatureEnabled(it) }
                    ?: false
            WoolworthsApplication.setInstantCardReplacement(instantCardReplacement)
            WoolworthsApplication.setVirtualTempCard(virtualTempCard)

            creditCardActivation?.apply {
                isEnabled = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
            }
            WoolworthsApplication.setCreditCardActivation(creditCardActivation)
            WoolworthsApplication.setClickAndCollect(clickAndCollect)
        }
    }

    companion object {

        const val APP_SERVER_ENVIRONMENT_KEY = "app_server_environment"
        const val APP_VERSION_KEY = "app_version"
    }
}
