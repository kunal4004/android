package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.AvailableTimeSlotsResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.PossibleAddressResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class CreditCardDeliveryInteractorImpl : CreditCardDeliveryContract.CreditCardDeliveryInteractor {

    override fun queryServiceValidateAddress(searchPhrase: String, productOfferingId: String, requestListener: IGenericAPILoaderView<Any>): Call<PossibleAddressResponse>? {
        return request(OneAppService.getPossibleAddress(searchPhrase, productOfferingId), requestListener)
    }

    override fun queryServiceAvailableTimeSlots(envelopeReference: String, productOfferingId: String, x: String, y: String, shipByDate: String, requestListener: IGenericAPILoaderView<Any>): Call<AvailableTimeSlotsResponse>? {
        return request(OneAppService.getAvailableTimeSlots(envelopeReference, productOfferingId, x, y, shipByDate), requestListener)
    }
}