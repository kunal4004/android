package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.data

import kotlinx.coroutines.flow.MutableStateFlow
import za.co.woolworths.financial.services.android.models.dto.account.PetInsuranceModel
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.schema.OfferProductType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountOfferKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_view_free_credit_report.ViewFreeCreditReportImpl
import javax.inject.Inject

interface IOfferSectionModel {
    fun buildAllOfferList(): MutableMap<AccountOfferKeys, CommonItem.OfferItem>
    fun buildInitialOfferList(): MutableMap<AccountOfferKeys, CommonItem.OfferItem?>
    fun getOfferProductByOfferKey(key: AccountOfferKeys): CommonItem.OfferItem
    fun constructMapOfMyOffers(
        mapOfMyProducts: MutableMap<String, AccountProductCardsGroup?>,
        petInsuranceState: MutableStateFlow<NetworkStatusUI<PetInsuranceModel>>
    ): MutableMap<AccountOfferKeys, CommonItem.OfferItem?>
}

class OfferSectionModel @Inject constructor(
    private val creditReportView: ViewFreeCreditReportImpl): IOfferSectionModel {

    override fun buildAllOfferList(): MutableMap<AccountOfferKeys, CommonItem.OfferItem> {
        return mutableMapOf(
            AccountOfferKeys.StoreCardApplyNow to OfferProductType.StoreCardApplyNow.value(),
            AccountOfferKeys.PersonalLoanApplyNow to OfferProductType.PersonalLoanApplyNow.value(),
            AccountOfferKeys.CreditCardApplyNow to OfferProductType.CreditCardApplyNow.value(),
            AccountOfferKeys.ViewApplicationStatus to OfferProductType.ViewApplicationStatus.value(),
            AccountOfferKeys.CreditReport to OfferProductType.ViewFreeCreditReport.value(),
            AccountOfferKeys.PetInsurance to OfferProductType.PetInsurance.value()
        )
    }

    override fun buildInitialOfferList(): MutableMap<AccountOfferKeys, CommonItem.OfferItem?> {
        return mutableMapOf(
            AccountOfferKeys.ViewApplicationStatus to OfferProductType.ViewApplicationStatus.value(false),
            AccountOfferKeys.CreditCardApplyNow to OfferProductType.BlackCreditCardApplyNow.value(),
            AccountOfferKeys.StoreCardApplyNow to OfferProductType.StoreCardApplyNow.value(),
            AccountOfferKeys.PersonalLoanApplyNow to OfferProductType.PersonalLoanApplyNow.value()
        )
    }

    override fun getOfferProductByOfferKey(key: AccountOfferKeys): CommonItem.OfferItem {
        val offers = buildAllOfferList()
        return offers[key] ?: OfferProductType.StoreCardApplyNow.value()
    }

    override fun constructMapOfMyOffers(
        mapOfMyProducts: MutableMap<String, AccountProductCardsGroup?>,
        petInsuranceState: MutableStateFlow<NetworkStatusUI<PetInsuranceModel>>
    ): MutableMap<AccountOfferKeys, CommonItem.OfferItem?> {

        // Create a mutable map of account offer keys to my offer items
        val mapOfMyOffers: MutableMap<AccountOfferKeys, CommonItem.OfferItem?> = mutableMapOf()
        val applyNowProducts = mutableListOf(AccountOfferKeys.StoreCardApplyNow, AccountOfferKeys.PersonalLoanApplyNow, AccountOfferKeys.CreditCardApplyNow)
        val inArrearsProducts : MutableList<AccountOfferKeys> = mutableListOf()

        // Create a mutable list of account offer keys that can be applied for
        val productToOfferMap = mapOf(
            AccountProductKeys.StoreCard to AccountOfferKeys.StoreCardApplyNow,
            AccountProductKeys.PersonalLoan to AccountOfferKeys.PersonalLoanApplyNow,
            AccountProductKeys.BlackCreditCard to AccountOfferKeys.CreditCardApplyNow
        )

        // Get the matching account offer key based on the account product key
        val matchedKeys = mapOfMyProducts.mapNotNull { (key, _) ->
            val productKey = AccountProductKeys.fromString(key)
            productToOfferMap[productKey]
        }

        // If there is a matching account offer key, remove it from the list of products that can be applied for
        val remainingKeys = applyNowProducts.filterNot { it in matchedKeys }

        val accountOfferKeys: MutableList<AccountOfferKeys> = (remainingKeys + inArrearsProducts).toMutableList()

        // Remove credit report view from list
        if (creditReportView.isVisible()){
            accountOfferKeys.add(AccountOfferKeys.CreditReport)
        }

        for (key in accountOfferKeys) {
            mapOfMyOffers += key to getOfferProductByOfferKey(key = key)
        }

        return mapOfMyOffers
    }
}