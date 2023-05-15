package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.logic


import za.co.woolworths.financial.services.android.models.AppConfigSingleton.creditCardDelivery
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigCreditCardDeliveryCardTypes
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.ABSACard
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.ProductDetails
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface CreditCardDeliveryRequestBuilder {
    fun getCreditCardAccount(userAccountResponse: UserAccountResponse): ProductDetails?
    fun getCard(account: ProductDetails?): ABSACard?
    fun isCardStatusPLCAndEnvelopNumberNotNull(absaCard: ABSACard?) : Boolean
    fun getCardTypesFromMobileConfig() : MutableList<ConfigCreditCardDeliveryCardTypes>
    fun isScheduleCreditCardDeliveryServiceTriggered(userAccountResponse: UserAccountResponse) : Boolean
    fun isAccountBinNumbersMatching(productDetails: ProductDetails?) : Boolean
    fun getProductOfferingIdAndEnvelopeNumber(userAccountResponse: UserAccountResponse): Pair<Int?, String?>
}

class CreditCardDeliveryRequestResponseBuilderImpl @Inject constructor() :
    CreditCardDeliveryRequestBuilder {

    override fun getCreditCardAccount(userAccountResponse: UserAccountResponse): ProductDetails? {
        return userAccountResponse.accountList?.singleOrNull {
            it.productGroupCode.equals(
                AccountProductKeys.BlackCreditCard.value,
                ignoreCase = true
            )
        }
    }

    override fun getCard(account: ProductDetails?): ABSACard? {
        return account?.cards?.firstOrNull()
    }

    override fun isCardStatusPLCAndEnvelopNumberNotNull(absaCard: ABSACard?): Boolean {
        return absaCard?.let {
            it.cardStatus.equals("PLC", ignoreCase = true)
                    && it.envelopeNumber?.isNotBlank() == true
        } ?: false
    }

    override fun getCardTypesFromMobileConfig(): MutableList<ConfigCreditCardDeliveryCardTypes> {
        return creditCardDelivery?.cardTypes ?: mutableListOf()
    }

    override fun isScheduleCreditCardDeliveryServiceTriggered(userAccountResponse: UserAccountResponse): Boolean {
        val account = getCreditCardAccount(userAccountResponse = userAccountResponse)
        val absaCard = getCard(account)
        val isCardStatusPLCAndEnvelopNumberNotNull =
            isCardStatusPLCAndEnvelopNumberNotNull(absaCard)
        return isCardStatusPLCAndEnvelopNumberNotNull && isAccountBinNumbersMatching(account)
    }

    override fun isAccountBinNumbersMatching(productDetails: ProductDetails?): Boolean {
        val cardTypes = getCardTypesFromMobileConfig()
        val accountNumberBin = productDetails?.accountNumberBin?.lowercase()
        return cardTypes.any {
            it.binNumber.equals(accountNumberBin, ignoreCase = true)
                    && Utils.isFeatureEnabled(it.minimumSupportedAppBuildNumber)
        }
    }

    override fun getProductOfferingIdAndEnvelopeNumber(userAccountResponse: UserAccountResponse): Pair<Int?, String?> {
        val account = getCreditCardAccount(userAccountResponse = userAccountResponse)
        val absaCard = getCard(account)
        return account?.productOfferingId to absaCard?.envelopeNumber
    }

}