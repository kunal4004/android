package za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance.apply

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import za.co.woolworths.financial.services.android.models.dto.account.AppGUIDModel
import za.co.woolworths.financial.services.android.models.dto.account.AppGUIDRequestType
import za.co.woolworths.financial.services.android.models.dto.account.getRequestBody
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.mapNetworkCallToViewStateFlow
import za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance.apply.data.IPetInsuranceApplyNowRepo
import za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance.apply.data.PetInsuranceApplyNowRepo
import javax.inject.Inject

@HiltViewModel
class PetInsuranceApplyNowViewModel @Inject constructor( repo: PetInsuranceApplyNowRepo) : ViewModel(),
    IPetInsuranceApplyNowRepo by repo {
    suspend fun getAppGUID() = mapNetworkCallToViewStateFlow {
        queryServicetAppGUID(getRequestBody(AppGUIDRequestType.PET_INSURANCE)) }
    var appGUIDResponse: MutableStateFlow<AppGUIDModel?> = MutableStateFlow(null)

}