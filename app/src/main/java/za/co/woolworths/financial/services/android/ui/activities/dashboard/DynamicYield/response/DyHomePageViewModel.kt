package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.response

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.repository.DyChooseVariationRepository
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.HomePageRequestEvent
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@HiltViewModel
class DyHomePageViewModel @Inject constructor(
    private val dyChooseVariationRepository: DyChooseVariationRepository
): ViewModel() {

    fun createDyRequest(chooseVariationRequestEvent: HomePageRequestEvent) {
        viewModelScope.launch {
            val response = dyChooseVariationRepository.getDyChooseVariationResponse(chooseVariationRequestEvent)
            if (response.status == Status.SUCCESS) {
                for (myData in response.data?.cookies!!) {
                    if (myData.name.equals("_dyid_server")) {
                        Utils.saveDyServerId(myData.value)
                        Utils.sessionDaoSaveDyServerId(SessionDao.KEY.DY_SERVER_ID, myData.value)
                    }else if (myData.name.equals("_dyjsession")) {
                        Utils.saveDySessionId(myData.value)
                        Utils.sessionDaoSaveDyServerId(SessionDao.KEY.DY_SESSION_ID, myData.value)
                    }
                }
            }
        }
    }
}