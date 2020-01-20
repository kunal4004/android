package za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.credit_card_activation.CreditCardActivationRequestBody
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService

class CreditCardActivationInteractorImpl : CreditCardActivationContract.CreditCardActivationInteractor {

    override fun executeCreditCardActivation(requestBody: CreditCardActivationRequestBody, onFinishListener: CreditCardActivationContract.CreditCardActivationInteractor.OnFinishListener) {
        request(OneAppService.activateCreditCardRequest(requestBody), onFinishListener)
    }


    private inline fun <reified RESPONSE_OBJECT> request(call: Call<RESPONSE_OBJECT>, requestListener: CreditCardActivationContract.CreditCardActivationInteractor.OnFinishListener) {
        val classType: Class<RESPONSE_OBJECT> = RESPONSE_OBJECT::class.java
        call.enqueue(CompletionHandler(object : RequestListener<RESPONSE_OBJECT> {
            override fun onSuccess(response: RESPONSE_OBJECT) {
                requestListener?.onSuccess(response)
            }

            override fun onFailure(error: Throwable?) {
                requestListener?.onFailure(error)
            }
        }, classType))
    }
}