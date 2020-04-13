package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.ScheduleDeliveryRequest
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class ScheduleDeliveryInteractorImpl : ScheduleDeliveryContract.ScheduleDeliveryInteractor {
    override fun queryScheduleDelivery(scheduleDeliveryRequest: ScheduleDeliveryRequest, productOfferingId: String, requestListener: IGenericAPILoaderView<Any>): Call<CreditCardDeliveryStatusResponse>? {
        return request(OneAppService.postScheduleDelivery(productOfferingId, scheduleDeliveryRequest), requestListener)
    }
}