package za.co.woolworths.financial.services.android.contracts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import za.co.woolworths.financial.services.android.models.dto.account.*

interface IAccountSalesContract {

    interface AccountSalesModel {
        fun getCreditCard(): MutableList<AccountSales>
        fun getStoreCard(): AccountSales
        fun getPersonalLoan(): AccountSales
    }
}