package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.AvailableTimeSlotsResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.PossibleAddressResponse

class ValidateAddressAndTimeSlotPresenterImpl(var mainView: ValidateAddressAndTimeSlotContract.ValidateAddressAndTimeSlotView?, var getInteractor: ValidateAddressAndTimeSlotContract.ValidateAddressAndTimeSlotInteractor) : ValidateAddressAndTimeSlotContract.ValidateAddressAndTimeSlotPresenter, IGenericAPILoaderView<Any> {

    override fun onDestroy() {
        mainView = null
    }

    override fun initValidateAddress(searchPhrase: String, productOfferingId: String, envelopeNumber: String) {
        getInteractor.queryServiceValidateAddress(searchPhrase, productOfferingId, this, envelopeNumber)
    }

    override fun initAvailableTimeSlots(envelopeReference: String, productOfferingId: String, x: String, y: String, shipByDate: String) {
        getInteractor.queryServiceAvailableTimeSlots(envelopeReference, productOfferingId, x, y, shipByDate, this)
    }

    override fun onSuccess(response: Any?) {
        with(response) {
            when (this) {
                is PossibleAddressResponse -> {
                    when (httpCode) {
                        200 -> mainView?.onValidateAddressSuccess(this)
                        440 -> mainView?.onSessionTimeout()
                        else -> mainView?.onValidateAddressFailure()
                    }
                }
                is AvailableTimeSlotsResponse -> {
                    when (httpCode) {
                        200 -> {
                            if (this.timeslots.isNullOrEmpty())
                                mainView?.onNoTimeSlotsAvailable()
                            else
                                mainView?.onAvailableTimeSlotsSuccess(this)
                        }
                        440 -> mainView?.onSessionTimeout()
                        else -> mainView?.onAvailableTimeSlotsFailure()
                    }
                }
                else -> throw RuntimeException("onSuccess:: unknown response $response")
            }
        }
    }

    override fun onFailure(error: Throwable?) {
        mainView?.onValidateAddressFailure()
    }

}