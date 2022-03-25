package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.CollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.ICollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.EligibilityImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.IEligibilityImpl
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import javax.inject.Inject


@HiltViewModel
class AccountProductsHomeViewModel @Inject constructor(
    screen: AccountProductLandingScreenStatus,
    account: AccountProductLandingDao,
    eligibilityImpl: EligibilityImpl,
    private val collectionRepository: CollectionRepository
) : ViewModel(),
    IAccountProductLandingDao by account,
    IAccountProductLandingScreen by screen,
    IEligibilityImpl by eligibilityImpl,
    ICollectionRepository by collectionRepository {

    val eligibilityPlanResponseLiveData: MutableLiveData<ViewState<EligibilityPlanResponse>> by lazy {
        MutableLiveData<ViewState<EligibilityPlanResponse>>()
    }

    suspend fun eligibilityPlanResponse(): Flow<ViewState<EligibilityPlanResponse>> {
        return getViewStateFlowForNetworkCall {
            val productGroupCode = getAccountProduct()?.productGroupCode
            queryServiceCheckCustomerEligibilityPlan(productGroupCode!!)
        }
    }
}


