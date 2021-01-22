package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.AvailableTimeSlotsResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.PossibleAddressResponse
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment

interface ValidateAddressAndTimeSlotContract {

    interface ValidateAddressAndTimeSlotView {
        fun startProgress()
        fun stopProgress(isFailed: Boolean = false)
        fun onValidateAddressSuccess(possibleAddressResponse: PossibleAddressResponse)
        fun onValidateAddressFailure()
        fun onInvalidAddress()
        fun onSessionTimeout()
        fun getProgressState(): ProgressStateFragment?
        fun onAvailableTimeSlotsSuccess(availableTimeSlotsResponse: AvailableTimeSlotsResponse)
        fun onAvailableTimeSlotsFailure()
        fun onNoTimeSlotsAvailable()
        fun getValidateAddress()
        fun getAvailableTimeSlots()
        fun restartProgress()
    }

    interface ValidateAddressAndTimeSlotPresenter {
        fun onDestroy()
        fun initValidateAddress(searchPhrase: String, productOfferingId: String, envelopeNumber: String)
        fun initAvailableTimeSlots(envelopeReference: String, productOfferingId: String, x: String, y: String, shipByDate: String)
    }

    interface ValidateAddressAndTimeSlotInteractor {
        fun queryServiceValidateAddress(searchPhrase: String, productOfferingId: String, requestListener: IGenericAPILoaderView<Any>, envelopeNumber: String): Call<PossibleAddressResponse>?
        fun queryServiceAvailableTimeSlots(envelopeReference: String, productOfferingId: String, x: String, y: String, shipByDate: String, requestListener: IGenericAPILoaderView<Any>): Call<AvailableTimeSlotsResponse>?
    }
}