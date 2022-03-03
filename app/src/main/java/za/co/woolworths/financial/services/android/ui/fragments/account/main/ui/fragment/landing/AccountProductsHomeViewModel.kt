package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingScreenStatus
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IAccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IAccountProductLandingScreen
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.CollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.ICollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.AccountApiResult
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

    private val _eligibilityPlanResponse by lazy { MutableLiveData<AccountApiResult<EligibilityPlanResponse>>() }
    val eligibilityPlanResponse by lazy { _eligibilityPlanResponse }

    fun fetchCollectionCheckEligibility(apiResult: (AccountApiResult<EligibilityPlanResponse>?) -> Unit) {
        val productGroupCode = getAccountProduct()?.productGroupCode
        viewModelScope.launch {
            fetchCollectionCheckEligibility(productGroupCode)?.collect { response ->
                _eligibilityPlanResponse.value = response
            }
        }
    }
}



