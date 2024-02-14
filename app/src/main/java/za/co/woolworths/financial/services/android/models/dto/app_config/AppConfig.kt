package za.co.woolworths.financial.services.android.models.dto.app_config // ktlint-disable package-name

import DynamicYieldConfig
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.ConnectOnline
import za.co.woolworths.financial.services.android.models.OutOfStock
import za.co.woolworths.financial.services.android.models.SearchApiSettings
import za.co.woolworths.financial.services.android.models.dto.RatingsAndReviews
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.ConfigAccountOptions
import za.co.woolworths.financial.services.android.models.dto.app_config.balance_protection_insurance.ConfigBalanceProtectionInsurance
import za.co.woolworths.financial.services.android.models.dto.app_config.chat.ConfigInAppChat
import za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase.ConfigCreditLimitIncrease
import za.co.woolworths.financial.services.android.models.dto.app_config.defaults.ConfigDefaults
import za.co.woolworths.financial.services.android.models.dto.app_config.device_security.ConfigDeviceSecurity
import za.co.woolworths.financial.services.android.models.dto.app_config.instant_card_replacement.ConfigInstantCardReplacement
import za.co.woolworths.financial.services.android.models.dto.app_config.native_checkout.ConfigNativeCheckout
import za.co.woolworths.financial.services.android.models.dto.app_config.whatsapp.ConfigWhatsApp

@Parcelize
data class AppConfig(
    var enviroment: ConfigEnvironment?,
    var expiry: ConfigExpiry?,
    var defaults: ConfigDefaults?,
    var absaBankingOpenApiServices: ConfigAbsaBankingOpenApiServices?,
    var payMyAccount: ConfigPayMyAccount?,
    var quickShopDefaultValues: ConfigQuickShopDefaultValues?,
    var instantCardReplacement: ConfigInstantCardReplacement?,
    var virtualTempCard: ConfigVirtualTempCard?,
    var applyNowLinks: ConfigApplyNowLinks?,
    var whitelistedDomainsForQRScanner: ArrayList<String>?,
    var sts: ConfigSts?,
    var creditCardActivation: ConfigCreditCardActivation?,
    var creditCardDelivery: ConfigCreditCardDelivery?,
    var whatsApp: ConfigWhatsApp?,
    var clickAndCollect: ConfigClickAndCollect?,
    var inAppChat: ConfigInAppChat?,
    var productDetailsPage: ConfigProductDetailsPage?,
    var creditView: ConfigCreditView?,
    var nativeCheckout: ConfigNativeCheckout?,
    var dashConfig: ConfigDashConfig?,
    var creditLimitIncrease: ConfigCreditLimitIncrease?,
    var liquor: ConfigLiquor?,
    var inAppReview: ConfigInAppReview?,
    var customerFeedback: ConfigCustomerFeedback?,
    var accountOptions: ConfigAccountOptions?,
    var deviceSecurity: ConfigDeviceSecurity?,
    var balanceProtectionInsurance: ConfigBalanceProtectionInsurance?,
    var virtualTryOn: ConfigVirtualTryOn?,
    var lowStockIndicator: ConfigLowStock?,
    var brandLandingPage: BrandLandingPage?,
    var toolTipSettings: TooltipSettings?,
    var enhanceSubstitution: EnhanceSubstitution?,
    var endlessAisle: EndlessAisle?,
    var ratingsAndReviews: RatingsAndReviews?,

    var bnplConfig: BnplConfig?,
    var searchApiSettings: SearchApiSettings?,
    var dynamicYieldConfig: DynamicYieldConfig?,
    var connectOnline: ConnectOnline?,
    var outOfStock: OutOfStock?,
    var backInStock: ConfigBackInStock
) : Parcelable
