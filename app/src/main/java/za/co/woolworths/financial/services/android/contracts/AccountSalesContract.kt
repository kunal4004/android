package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.account.AccountSales
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState


interface AccountSalesContract {

    interface AccountSalesView {
        fun displayHeaderItems(headerItems: AccountSales)
        fun hideCardCollectionLayout()
        fun displayAccountSalesBlackInfo(storeCard: AccountSales)
        fun displayCreditCardFrontUI(position: Int)
        fun displayCreditCard(goldCreditCard: AccountSales, blackCreditCard: AccountSales, position: Int)
    }

    interface AccountSalesPresenter {
        fun switchAccountSalesProduct(applyNowState: ApplyNowState)
    }

    interface AccountSalesModel {
        fun getCreditCard(): MutableList<AccountSales>
        fun getStoreCard(): AccountSales
        fun getPersonalLoan(): AccountSales
    }
}