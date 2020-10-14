package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.ScheduleDeliveryRequest
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment

interface ScheduleDeliveryContract {
    interface ScheduleDeliverView {
        fun startProgress()
        fun onScheduleDeliverySuccess(creditCardDeliveryStatusResponse: CreditCardDeliveryStatusResponse)
        fun onScheduleDeliveryFailure()
        fun onSessionTimeout()
        fun getProgressState(): ProgressStateFragment?
        fun postScheduleDelivery()
        fun retryScheduleDelivery()
    }

    interface ScheduleDeliveryPresenter {
        fun onDestroy()
        fun initScheduleDelivery(productOfferingId: String, envelopeNumber: String, schedule: Boolean, bookingReference: String, scheduleDeliveryRequest: ScheduleDeliveryRequest)
    }

    interface ScheduleDeliveryInteractor {
        fun queryScheduleDelivery(productOfferingId: String, envelopeNumber: String, schedule: Boolean, bookingReference: String, scheduleDeliveryRequest: ScheduleDeliveryRequest, requestListener: IGenericAPILoaderView<Any>): Call<CreditCardDeliveryStatusResponse>?
    }
}