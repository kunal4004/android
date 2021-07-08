package za.co.woolworths.financial.services.android.startup.utils

import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.AbsaBankingOpenApiServices
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.models.dto.chat.Collections
import za.co.woolworths.financial.services.android.models.dto.chat.CustomerService
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.InAppChat
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.startup.viewmodel.StartupViewModel
import za.co.woolworths.financial.services.android.util.Utils

/**
 * Created by Kunal Uttarwar on 23/2/21.
 */

/**
 * Returns Status with stream of data.
 */
data class ConfigResource(val responseStatus: ResponseStatus, val data: ConfigResponse?, val message: String?) {

    companion object {

        fun success(data: ConfigResponse): ConfigResource {
            return ConfigResource(ResponseStatus.SUCCESS, data, null)
        }

        fun  error(msg: String, data: ConfigResponse?): ConfigResource {
            return ConfigResource(ResponseStatus.ERROR, data, msg)
        }

        fun loading(data: ConfigResponse?): ConfigResource {
            return ConfigResource(ResponseStatus.LOADING, data, null)
        }

        fun persistGlobalConfig(response: ConfigResponse?, startupViewModel: StartupViewModel) {
            response?.configs?.apply {

                enviroment?.apply {
                    startupViewModel.splashScreenText = splashScreenText
                    startupViewModel.isSplashScreenDisplay = splashScreenDisplay
                    startupViewModel.isSplashScreenPersist = splashScreenPersist

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
                    WoolworthsApplication.setFirebaseUserPropertiesForDelinquentProductGroupCodes(firebaseUserPropertiesForDelinquentProductGroupCodes)
                }

                dashConfig?.apply {
                    minimumSupportedAppBuildNumber.let { isEnabled = Utils.isFeatureEnabled(it) }
                    WoolworthsApplication.getInstance().dashConfig = this
                }

                whatsApp?.apply {
                    showWhatsAppButton = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
                    WoolworthsApplication.setWhatsAppConfig(this)
                }

                WoolworthsApplication.setPayMyAccountOption(payMyAccount)

                WoolworthsApplication.setQuickShopDefaultValues(quickShopDefaultValues)
                WoolworthsApplication.setWhitelistedDomainsForQRScanner(whitelistedDomainsForQRScanner)
                WoolworthsApplication.setStsValues(sts)
                WoolworthsApplication.setApplyNowLink(applyNowLinks)

                var absaBankingOpenApiServices: AbsaBankingOpenApiServices? = absaBankingOpenApiServices
                if (absaBankingOpenApiServices == null) {
                    absaBankingOpenApiServices = AbsaBankingOpenApiServices(false, "", "", "", "")
                } else {
                    absaBankingOpenApiServices.isEnabled = Utils.isFeatureEnabled(absaBankingOpenApiServices.minimumSupportedAppBuildNumber)
                }


                var inAppChat: InAppChat? = inAppChat
                if (inAppChat == null) {
                    inAppChat = InAppChat("", "", "", "", Collections("", "", "", "", "", mutableListOf()), CustomerService("", "", "", "", "", mutableListOf()), null, mutableListOf())
                } else {
                    inAppChat.isEnabled = Utils.isFeatureEnabled(inAppChat.minimumSupportedAppBuildNumber)
                }

                val virtualTempCard = virtualTempCard
                if (virtualTempCard != null) {
                    virtualTempCard.isEnabled = Utils.isFeatureEnabled(virtualTempCard.minimumSupportedAppBuildNumber)
                }

                contactUs?.let { WoolworthsApplication.setContactUsDetails(it) }

                WoolworthsApplication.setInAppChat(inAppChat)

                WoolworthsApplication.setAbsaBankingOpenApiServices(absaBankingOpenApiServices)

                instantCardReplacement?.isEnabled = instantCardReplacement?.minimumSupportedAppBuildNumber?.let { Utils.isFeatureEnabled(it) }
                        ?: false
                WoolworthsApplication.setInstantCardReplacement(instantCardReplacement)
                WoolworthsApplication.setVirtualTempCard(virtualTempCard)

                creditCardActivation?.apply {
                    isEnabled = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
                }
                WoolworthsApplication.setCreditCardActivation(creditCardActivation)
                WoolworthsApplication.setCreditCardDelivery(creditCardDelivery)
                WoolworthsApplication.setClickAndCollect(clickAndCollect)
                WoolworthsApplication.setProductDetailsPage(productDetailsPage)

                creditView?.apply {
                    isEnabled = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
                    WoolworthsApplication.setCreditView(creditView)
                }

                creditLimitIncrease?.apply {
                    WoolworthsApplication.getInstance().setCreditLimitsIncrease(this)
                }

                inAppReview?.apply {
                    isEnabled = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
                    WoolworthsApplication.setInAppReview(this)
                }
                nativeCheckout.apply {
                    WoolworthsApplication.setNativeCheckout(nativeCheckout)
                }
            }
        }
    }
}