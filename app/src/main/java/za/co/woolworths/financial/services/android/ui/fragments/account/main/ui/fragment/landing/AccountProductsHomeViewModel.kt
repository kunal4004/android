package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.CollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard.ICollectionRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.EligibilityImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.IEligibilityImpl
import javax.inject.Inject

@HiltViewModel
class AccountProductsHomeViewModel @Inject constructor(screen: AccountProductLandingScreenStatus, account: AccountProductLandingDao, eligibilityImpl: EligibilityImpl, private val collectionRepository: CollectionRepository) : ViewModel(), IAccountProductLandingDao by account, IAccountProductLandingScreen by screen, IEligibilityImpl by eligibilityImpl, ICollectionRepository by collectionRepository {


    suspend fun eligibilityPlanResponse() = getViewStateFlowForNetworkCall { queryServiceCheckCustomerEligibilityPlan() }
}
