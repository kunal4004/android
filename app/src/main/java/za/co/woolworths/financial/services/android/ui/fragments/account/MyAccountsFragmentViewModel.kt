package za.co.woolworths.financial.services.android.ui.fragments.account

import androidx.lifecycle.ViewModel
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.apply_now.ViewApplicationStatusImpl

class MyAccountsFragmentViewModel : ViewModel() {

    var myAccountsPresenter : MyAccountsPresenter? = null

    fun getAccountPresenter(accountsResponse: AccountsResponse?): MyAccountsPresenter? {
        myAccountsPresenter =  MyAccountsPresenter(ViewApplicationStatusImpl(accountsResponse = accountsResponse))
        return myAccountsPresenter
    }
}