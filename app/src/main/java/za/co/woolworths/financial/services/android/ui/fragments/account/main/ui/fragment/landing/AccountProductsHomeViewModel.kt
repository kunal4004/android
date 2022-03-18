package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.CollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.ICollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.*
import javax.inject.Inject

@HiltViewModel
class AccountProductsHomeViewModel @Inject constructor(
    screen: AccountProductLandingScreenStatus,
    account: AccountProductLandingDao,
    private val collectionRepository: CollectionRepository
) : ViewModel(),
    IAccountProductLandingDao by account,
    IAccountProductLandingScreen by screen,
    ICollectionRepository by collectionRepository {

    fun queryServiceCheckCustomerEligibilityPlan() {
        val productGroupCode = getAccountProduct()?.productGroupCode
        viewModelScope.launch {
            queryServiceCheckCustomerEligibilityPlan(productGroupCode)?.collect { response ->
               // _eligibilityPlanResponse.value = response
            }
        }
    }
}



