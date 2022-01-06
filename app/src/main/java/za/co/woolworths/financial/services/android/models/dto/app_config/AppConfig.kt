package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.ConfigAccountOptions
import za.co.woolworths.financial.services.android.models.dto.app_config.balance_protection_insurance.ConfigBalanceProtectionInsurance
import za.co.woolworths.financial.services.android.models.dto.app_config.chat.ConfigInAppChat
import za.co.woolworths.financial.services.android.models.dto.app_config.contact_us.ConfigContactUs
import za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase.ConfigCreditLimitIncrease
import za.co.woolworths.financial.services.android.models.dto.app_config.defaults.ConfigDefaults
import za.co.woolworths.financial.services.android.models.dto.app_config.device_security.ConfigDeviceSecurity
import za.co.woolworths.financial.services.android.models.dto.app_config.instant_card_replacement.ConfigInstantCardReplacement
import za.co.woolworths.financial.services.android.models.dto.app_config.native_checkout.ConfigNativeCheckout
import za.co.woolworths.financial.services.android.models.dto.app_config.whatsapp.ConfigWhatsApp
import java.util.*

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
    var contactUs: ArrayList<ConfigContactUs>?,
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
    var virtualTryOn: ConfigVirtualTryOn?
) : Parcelable