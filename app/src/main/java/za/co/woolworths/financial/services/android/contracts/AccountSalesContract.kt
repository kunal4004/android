package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.account.AccountSales
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.CardHeader


interface AccountSalesContract {

    interface AccountSalesView {
        fun displayHeaderItems(headerItems: CardHeader)
        fun displayAccountSalesBlackInfo(storeCard: AccountSales)
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