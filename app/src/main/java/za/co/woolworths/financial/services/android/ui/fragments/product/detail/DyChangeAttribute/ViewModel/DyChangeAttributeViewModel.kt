package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Repository.DyReportEventRepository
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import javax.inject.Inject

@HiltViewModel
class DyChangeAttributeViewModel @Inject constructor(
    private val dyReportEventRepository: DyReportEventRepository
): ViewModel() {

    fun createDyChangeAttributeRequest(reportEventRequest: PrepareChangeAttributeRequestEvent) {
        viewModelScope.launch {
            val response = dyReportEventRepository.getDyReportEventResponse(reportEventRequest)
            if (response.status == Status.SUCCESS) {
                var value = response.data?.response?.desc
            }
        }
    }
}