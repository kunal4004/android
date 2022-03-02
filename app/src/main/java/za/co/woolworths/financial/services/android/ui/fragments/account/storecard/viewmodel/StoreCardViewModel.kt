package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.usecase.*
import javax.inject.Inject

@HiltViewModel
class StoreCardViewModel @Inject constructor(
    private val accountProduct: AccountProductLandingDao,
    private val accountOptions: AccountOptionsImpl
) : ViewModel(), IAccountProductLandingDao by accountProduct,
    IAccountOptions by accountOptions {
    init {
        balanceProtectionInsurance()
        isDebitOrderActive()
    }
}