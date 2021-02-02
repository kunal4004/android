package za.co.woolworths.financial.services.android.contracts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import za.co.woolworths.financial.services.android.models.dto.account.*

interface IAccountSalesContract {

    interface AccountSalesView {
        fun displayHeaderItems(cardHeader: CardHeader?)
        fun displayAccountSalesBlackInfo(storeCard: AccountSales)
        fun displayCreditCard(fragmentList: Map<String, Fragment>?, position: Int)
        fun displayCreditCardFrontUI(position: Int)
    }

    interface AccountSalesPresenter {
        fun switchAccountSalesProduct()
        fun setAccountSalesIntent(intent: Intent?)
        fun getApplyNowState(): ApplyNowState?
        fun isCreditCardProduct(): Boolean
        fun bottomSheetBehaviourHeight(): Int
        fun bottomSheetPeekHeight() : Int
    }

    interface AccountSalesModel {
        fun getCreditCard(): MutableList<AccountSales>
        fun getFragment(): Map<String, Fragment>?
        fun getStoreCard(): AccountSales
        fun getPersonalLoan(): AccountSales
    }
}