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
class DyChooseVariationCallViewModel @Inject constructor(
    private val dyChooseVariationRepository: DyChooseVariationRepository
): ViewModel() {

    fun createDyRequest(chooseVariationRequestEvent: HomePageRequestEvent) {
        viewModelScope.launch {
            val response = dyChooseVariationRepository.getDyChooseVariationResponse(chooseVariationRequestEvent)
        }
    }
}