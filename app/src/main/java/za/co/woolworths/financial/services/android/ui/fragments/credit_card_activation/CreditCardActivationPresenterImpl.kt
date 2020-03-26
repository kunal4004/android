package za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation

import za.co.woolworths.financial.services.android.models.dto.credit_card_activation.CreditCardActivationRequestBody
import za.co.woolworths.financial.services.android.models.dto.credit_card_activation.CreditCardActivationResponse

class CreditCardActivationPresenterImpl(var mainView: CreditCardActivationContract.CreditCardActivationView?, var getInteractor: CreditCardActivationContract.CreditCardActivationInteractor) : CreditCardActivationContract.CreditCardActivationPresenter, CreditCardActivationContract.CreditCardActivationInteractor.OnFinishListener {


    override fun onDestroy() {
        mainView = null
    }

    override fun initCreditCardActivation(absaCardToken: String) {
        getInteractor.executeCreditCardActivation(CreditCardActivationRequestBody(absaCardToken), this)
    }

    override fun onSuccess(response: Any?) {
        with(response) {
            when (this) {
                is CreditCardActivationResponse -> {
                    when (httpCode) {
                        200 -> mainView?.onCreditCardActivationSuccess()
                        440 -> mainView?.onSessionTimeout()
                        else -> mainView?.onCreditCardActivationFailure()
                    }
                }
                else -> throw RuntimeException("onSuccess:: unknown response $response")
            }
        }
    }

    override fun onFailure(error: Throwable?) {
        mainView?.onCreditCardActivationFailure()
    }
}