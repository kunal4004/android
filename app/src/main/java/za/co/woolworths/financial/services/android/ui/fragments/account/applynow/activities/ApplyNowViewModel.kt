package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.activities

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ApplyNowModel
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.ApplyNowBottomSheetImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.IApplyNowBottomSheetImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.data.ApplyNowRepo
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.data.IApplyNowRepo
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.getViewStateFlowForNetworkCall
import javax.inject.Inject

@HiltViewModel
class ApplyNowViewModel @Inject constructor(val bottomSheet: ApplyNowBottomSheetImpl,repo: ApplyNowRepo) : ViewModel(),
    IApplyNowBottomSheetImpl by bottomSheet ,IApplyNowRepo by repo{

    suspend fun applyNowResponse(contentId:String) = getViewStateFlowForNetworkCall { queryServiceApplyNow(contentId) }

    var applyNowResponse: MutableStateFlow<ApplyNowModel?> = MutableStateFlow(null)
}
