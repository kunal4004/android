package za.co.woolworths.financial.services.android.models

import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.RatingsAndReviews
import za.co.woolworths.financial.services.android.models.dto.app_config.*
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.ConfigAccountOptions
import za.co.woolworths.financial.services.android.models.dto.app_config.balance_protection_insurance.ConfigBalanceProtectionInsurance
import za.co.woolworths.financial.services.android.models.dto.app_config.chat.ConfigCollections
import za.co.woolworths.financial.services.android.models.dto.app_config.chat.ConfigCustomerService
import za.co.woolworths.financial.services.android.models.dto.app_config.chat.ConfigInAppChat
import za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase.ConfigCreditLimitIncrease
import za.co.woolworths.financial.services.android.models.dto.app_config.defaults.ConfigUserPropertiesForDelinquentCodes
import za.co.woolworths.financial.services.android.models.dto.app_config.device_security.ConfigDeviceSecurity
import za.co.woolworths.financial.services.android.models.dto.app_config.instant_card_replacement.ConfigInstantCardReplacement
import za.co.woolworths.financial.services.android.models.dto.app_config.native_checkout.ConfigNativeCheckout
import za.co.woolworths.financial.services.android.models.dto.app_config.whatsapp.ConfigWhatsApp
import za.co.woolworths.financial.services.android.models.repository.AppConfigRepository
import za.co.woolworths.financial.services.android.util.Utils

object AppConfigSingleton {
    var storeCardBlockReasons: List<Map<String, String>>? = null
    var whatsApp: ConfigWhatsApp? = null
    var mPayMyAccount: ConfigPayMyAccount? = null
    var inAppChat: ConfigInAppChat? = null
    var isProductItemForLiquorInventoryPending = false
    var productItemForLiquorInventory: ProductList? = null

    var applyNowLink: ConfigApplyNowLinks? = null
    var registrationTCLink: String? = null
    var faqLink: String? = null
    var wrewardsLink: String? = null
    var rewardingLink: String? = null
    var howToSaveLink: String? = null
    var wrewardsTCLink: String? = null
    var cartCheckoutLink: String? = null

    var ssoRedirectURI: String? = null
    var stsURI: String? = null
    var ssoRedirectURILogout: String? = null
    var ssoUpdateDetailsRedirectUri: String? = null
    var wwTodayURI: String? = null

    var absaBankingOpenApiServices: ConfigAbsaBankingOpenApiServices? = null
    var quickShopDefaultValues: ConfigQuickShopDefaultValues? = null
    var instantCardReplacement: ConfigInstantCardReplacement? = null
    var virtualTempCard: ConfigVirtualTempCard? = null
    var whitelistedDomainsForQRScanner: ArrayList<String>? = null
    var stsValues: ConfigSts? = null
    var creditCardActivation: ConfigCreditCardActivation? = null
    var clickAndCollect: ConfigClickAndCollect? = null
    var firebaseUserPropertiesForDelinquentProductGroupCodes: ConfigUserPropertiesForDelinquentCodes? =
        null
    var creditCardDelivery: ConfigCreditCardDelivery? = null
    var customerFeedback: ConfigCustomerFeedback? = null
    var productDetailsPage: ConfigProductDetailsPage? = null
    var creditView: ConfigCreditView? = null
    var nativeCheckout: ConfigNativeCheckout? = null
    var dashConfig: ConfigDashConfig? = null
    var creditLimitIncrease: ConfigCreditLimitIncrease? = null
    var isBadgesRequired = false
    var inAppReview: ConfigInAppReview? = null
    var liquor: ConfigLiquor? = null
    var accountOptions: ConfigAccountOptions? = null
    var deviceSecurity: ConfigDeviceSecurity? = null
    var balanceProtectionInsurance: ConfigBalanceProtectionInsurance? = null
    var virtualTryOn: ConfigVirtualTryOn? = null
    var brandLandingPage: BrandLandingPage? = null
    var logPublicKey: String? = null
    var authenticVersionStamp: String? = ""
    var lowStock: ConfigLowStock? = null
    var tooltipSettings: TooltipSettings? = null
    var ratingsAndReviews: RatingsAndReviews? = null

    @JvmStatic
    var searchApiSettings: SearchApiSettings? = null
    var glassBox: GlassBox? = null
    var bnplConfig: BnplConfig? = null
    var connectOnline: ConnectOnline? = null

    init {
        initialiseFromCache()
    }

    fun initialiseFromCache() {
        AppConfigRepository().getAppConfigData()?.let { appConfig ->
            appConfig.enviroment?.let { env ->
                storeCardBlockReasons = env.storeCardBlockReasons
                ssoRedirectURI = env.ssoRedirectURI
                stsURI = env.stsURI
                ssoRedirectURILogout = env.ssoRedirectURILogout
                ssoUpdateDetailsRedirectUri = env.ssoUpdateDetailsRedirectUri
                wwTodayURI = env.wwTodayURI
                authenticVersionStamp = env.authenticVersionStamp ?: ""

                WoolworthsApplication.getInstance().wGlobalState.startRadius =
                    env.storeStockLocatorConfigStartRadius ?: 0
                WoolworthsApplication.getInstance().wGlobalState.endRadius =
                    env.storeStockLocatorConfigEndRadius ?: 0
            }

            appConfig.defaults?.let { defaults ->
                registrationTCLink = defaults.registerTCLink
                faqLink = defaults.faqLink
                logPublicKey = defaults.logPublicKey
                wrewardsLink = defaults.wrewardsLink
                rewardingLink = defaults.rewardingLink
                howToSaveLink = defaults.howtosaveLink
                wrewardsTCLink = defaults.wrewardsTCLink
                cartCheckoutLink = defaults.cartCheckoutLink
                firebaseUserPropertiesForDelinquentProductGroupCodes =
                    defaults.firebaseUserPropertiesForDelinquentProductGroupCodes
            }

            appConfig.dashConfig?.apply {
                dashConfig = this
            }

            appConfig.whatsApp?.apply {
                showWhatsAppButton = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
                whatsApp = this
            }

            mPayMyAccount = appConfig.payMyAccount

            quickShopDefaultValues = appConfig.quickShopDefaultValues
            connectOnline = appConfig.connectOnline
            whitelistedDomainsForQRScanner = appConfig.whitelistedDomainsForQRScanner
            stsValues = appConfig.sts
            applyNowLink = appConfig.applyNowLinks

            absaBankingOpenApiServices = appConfig.absaBankingOpenApiServices
            if (absaBankingOpenApiServices == null) {
                absaBankingOpenApiServices = ConfigAbsaBankingOpenApiServices(
                    false,
                    "",
                    "",
                    "",
                    0,
                )
            } else {
                absaBankingOpenApiServices?.isEnabled =
                    Utils.isFeatureEnabled(absaBankingOpenApiServices?.minimumSupportedAppBuildNumber)
            }

            inAppChat = appConfig.inAppChat
            if (inAppChat == null) {
                inAppChat = ConfigInAppChat(
                    0,
                    "",
                    "",
                    "",
                    ConfigCollections(
                        "",
                        "",
                        "",
                        "",
                        "",
                        mutableListOf(),
                    ),
                    ConfigCustomerService(
                        "",
                        "",
                        "",
                        "",
                        "",
                        mutableListOf(),
                    ),
                    null,
                    mutableListOf(),
                )
            } else {
                inAppChat?.isEnabled =
                    Utils.isFeatureEnabled(inAppChat?.minimumSupportedAppBuildNumber)
            }

            virtualTempCard = appConfig.virtualTempCard
            if (virtualTempCard != null) {
                virtualTempCard?.isEnabled =
                    Utils.isFeatureEnabled(virtualTempCard?.minimumSupportedAppBuildNumber)
            }

            absaBankingOpenApiServices = appConfig.absaBankingOpenApiServices

            instantCardReplacement = appConfig.instantCardReplacement
            instantCardReplacement?.isEnabled =
                instantCardReplacement?.minimumSupportedAppBuildNumber?.let {
                    Utils.isFeatureEnabled(it)
                }
                    ?: false

            appConfig.creditCardActivation?.apply {
                isEnabled = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
            }
            creditCardActivation = appConfig.creditCardActivation
            creditCardDelivery = appConfig.creditCardDelivery
            clickAndCollect = appConfig.clickAndCollect
            productDetailsPage = appConfig.productDetailsPage

            customerFeedback = appConfig.customerFeedback

            appConfig.creditView?.apply {
                isEnabled = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
                creditView = this
            }

            creditLimitIncrease = appConfig.creditLimitIncrease
            nativeCheckout = appConfig.nativeCheckout
            appConfig.inAppReview?.apply {
                isEnabled = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
                inAppReview = this
            }

            liquor = appConfig.liquor
            accountOptions = appConfig.accountOptions
            deviceSecurity = appConfig.deviceSecurity
            balanceProtectionInsurance = appConfig.balanceProtectionInsurance

            appConfig.virtualTryOn?.apply {
                minimumSupportedAppBuildNumber.let { isEnabled = Utils.isFeatureEnabled(it) }
                virtualTryOn = this
            }

            appConfig.lowStockIndicator?.apply {
                minimumSupportedAppBuildNumber.let {
                    isEnabled = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
                    lowStock = this
                }
            }

            appConfig.brandLandingPage?.apply {
                minimumSupportedAppBuildNumber.let { isEnabled = Utils.isFeatureEnabled(it) }
                brandLandingPage = this
            }

            this.tooltipSettings = appConfig.toolTipSettings

            appConfig.ratingsAndReviews?.apply {
                minimumSupportedAppBuildNumber.let { isEnabled = Utils.isFeatureEnabled(it) }
                ratingsAndReviews = this
            }

            appConfig.glassBox?.apply {
                minimumSupportedAppBuildNumber.let {
                    isEnabled = Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
                    glassBox = this
                }
            }
            appConfig.bnplConfig?.apply {
                minimumSupportedAppBuildNumber.let {
                    isBnplRequiredInThisVersion =
                        Utils.isFeatureEnabled(minimumSupportedAppBuildNumber)
                    bnplConfig = this
                }
            }
            appConfig.searchApiSettings?.apply {
                searchApiSettings = this
            }
            appConfig.connectOnline?.apply {
                connectOnline = this
            }
        }
    }
}
