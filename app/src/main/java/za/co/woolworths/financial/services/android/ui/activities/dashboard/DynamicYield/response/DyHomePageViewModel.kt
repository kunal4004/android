package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.response

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.repository.DyChooseVariationRepository
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.HomePageRequestEvent
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Utils.DY_SERVERID
import za.co.woolworths.financial.services.android.util.Utils.DY_SESSIONID
import javax.inject.Inject

@HiltViewModel
class DyHomePageViewModel @Inject constructor(
    private val dyChooseVariationRepository: DyChooseVariationRepository
): ViewModel() {

    fun createDyRequest(chooseVariationRequestEvent: HomePageRequestEvent) {
        viewModelScope.launch {
            val response = dyChooseVariationRepository.getDyChooseVariationResponse(chooseVariationRequestEvent)
            if (response.status == Status.SUCCESS) {
                response.data?.cookies?.forEach { myData ->
                    myData.let {
                        when (myData.name) {
                            DY_SERVERID -> Utils.saveDyServerId(myData.value)
                            DY_SESSIONID -> Utils.saveDySessionId(myData.value)
                        }
                    }
                }
            }
        }
    }
}