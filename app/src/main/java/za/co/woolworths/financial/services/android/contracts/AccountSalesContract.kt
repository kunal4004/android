package za.co.woolworths.financial.services.android.contracts

import androidx.fragment.app.Fragment
import za.co.woolworths.financial.services.android.models.dto.account.AccountSales
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.CardHeader


interface AccountSalesContract {

    interface AccountSalesView {
        fun displayHeaderItems(cardHeader: CardHeader?)
        fun displayAccountSalesBlackInfo(storeCard: AccountSales)
        fun displayCreditCard(fragmentList : Map<String, Fragment>?, position: Int)
        fun displayCreditCardFrontUI(position: Int)
    }

    interface AccountSalesPresenter {
        fun switchAccountSalesProduct(applyNowState: ApplyNowState)
    }

    interface AccountSalesModel {
        fun getCreditCard(): MutableList<AccountSales>
        fun getFragment(): Map<String, Fragment>?
        fun getStoreCard(): AccountSales
        fun getPersonalLoan(): AccountSales
    }
}