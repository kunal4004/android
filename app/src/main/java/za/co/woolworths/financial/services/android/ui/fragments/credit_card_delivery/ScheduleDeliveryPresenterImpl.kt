package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.ScheduleDeliveryRequest

class ScheduleDeliveryPresenterImpl(var mainView: ScheduleDeliveryContract.ScheduleDeliverView?, var getInteractor: ScheduleDeliveryContract.ScheduleDeliveryInteractor) : ScheduleDeliveryContract.ScheduleDeliveryPresenter, IGenericAPILoaderView<Any> {

    override fun onDestroy() {
        mainView = null
    }

    override fun initScheduleDelivery(productOfferingId: String, envelopeNumber: String, schedule: Boolean, bookingReference: String, scheduleDeliveryRequest: ScheduleDeliveryRequest) {
        getInteractor.queryScheduleDelivery(productOfferingId, envelopeNumber, schedule, bookingReference, scheduleDeliveryRequest, this)
    }

    override fun onSuccess(response: Any?) {
        with(response) {
            when (this) {
                is CreditCardDeliveryStatusResponse -> {
                    when (httpCode) {
                        200 -> mainView?.onScheduleDeliverySuccess(this)
                        440 -> mainView?.onSessionTimeout()
                        else -> mainView?.onScheduleDeliveryFailure()
                    }
                }
                else -> throw RuntimeException("onSuccess:: unknown response $response")
            }
        }
    }

    override fun onFailure(error: Throwable?) {
        mainView?.onScheduleDeliveryFailure()
    }

}