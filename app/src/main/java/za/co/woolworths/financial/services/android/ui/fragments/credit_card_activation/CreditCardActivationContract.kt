package za.co.woolworths.financial.services.android.ui.fragments.credit_card_activation

import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.credit_card_activation.CreditCardActivationRequestBody
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProgressStateFragment

interface CreditCardActivationContract {

    interface CreditCardActivationView {
        fun startProgress()
        fun onCreditCardActivationSuccess()
        fun onCreditCardActivationFailure()
        fun onSessionTimeout()
        fun activateCreditCard()
        fun getProgressState(): ProgressStateFragment?
        fun onRetryActivation()

    }

    interface CreditCardActivationPresenter {
        fun onDestroy()
        fun initCreditCardActivation(absaCardToken: String)
    }

    interface CreditCardActivationInteractor {

        interface OnFinishListener : IResponseListener<Any>

        fun executeCreditCardActivation(requestBody: CreditCardActivationRequestBody, onFinishListener: OnFinishListener)
    }
}