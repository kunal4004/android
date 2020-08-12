package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.GlobalScope
import za.co.absa.openbankingapi.woolworths.integration.dto.PayUResponse
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.AddCardResponse
import za.co.woolworths.financial.services.android.models.dto.PayUPay
import za.co.woolworths.financial.services.android.models.dto.PayUPaymentMethod
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.util.ConnectionBroadcastReceiver
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import java.net.ConnectException

class PMAProcessRequestFragment : ProcessYourRequestFragment() {

    private var cardDetailArgs: AddCardResponse? = null
    private var accountArgs: Account? = null
    private var navController: NavController? = null
    private var hasPMAPostPayUPayCompleted: Boolean = false

    val args: PMAProcessRequestFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountArgs = args.account
        cardDetailArgs = args.tokenReceivedFromAddCard

        navController = Navigation.findNavController(view)

        circularProgressListener({}, {}) // onSuccess(), onFailure()

        activity?.let { activity ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(activity, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    when (hasConnection && !hasPMAPostPayUPayCompleted) {
                        true -> postPayUMethod()
                        else -> return
                    }
                }
            })
        }
    }

    private fun postPayUMethod() {
        val payURequestBody = payURequestBody(cardDetailArgs, accountArgs)
        startSpinning()
        request(OneAppService.queryServicePostPayU(payURequestBody), object : IGenericAPILoaderView<Any> {

            override fun onSuccess(response: Any?) {
                if (!isAdded) return
                GlobalScope.doAfterDelay(200) {
                    hasPMAPostPayUPayCompleted = true
                    (response as? PayUResponse)?.apply {
                        when (httpCode) {
                            200 -> {
                                isAPICallSuccessFul = true
                                navController?.navigate(PMAProcessRequestFragmentDirections.actionPMAProcessRequestFragmentToProcessPaymentFailureFragment())
                            }
                            440 -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response.stsParams, activity)
                            500 -> {
                                isAPICallSuccessFul = false
                                if (response.response.code.startsWith("P0"))
                                    navController?.navigate(PMAProcessRequestFragmentDirections.actionPMAProcessRequestFragmentToProcessPaymentFailureFragment())
                                else
                                    showError(response)
                            }
                            else -> {
                                isAPICallSuccessFul = false
                                showError(response)
                            }
                        }
                    }
                    stopSpinning(true)
                }
            }

            override fun onFailure(error: Throwable?) {
                hasPMAPostPayUPayCompleted = false
                activity?.apply {
                    if (!isAdded) return
                    stopSpinning(false)
                    if (error is ConnectException) {
                        ErrorHandlerView(this).showToast()
                    }
                }
            }
        })
    }

    private fun showError(response: PayUResponse) {
        activity?.supportFragmentManager?.let { fragmentManager ->
            Utils.showGeneralErrorDialog(fragmentManager, response.response.desc ?: "")
        }
    }

    private fun payURequestBody(cardDetailArgs: AddCardResponse?, accountArgs: Account?): PayUPay {

        val amountEntered = (activity as? PayMyAccountActivity)?.amountEntered ?: 0

        val creditCardCVV = cardDetailArgs?.card?.cvv ?: ""
        val token = cardDetailArgs?.token ?: "0"
        val type = cardDetailArgs?.card?.type ?: ""
        val isSaveCardChecked = cardDetailArgs?.saveChecked ?: false
        val currency = "ZAR"

        val accountNumber = accountArgs?.accountNumber ?: "0"
        val productOfferingId = accountArgs?.productOfferingId ?: 0
        val paymentMethod = PayUPaymentMethod(token, creditCardCVV, type)

        return PayUPay(amountEntered, currency, productOfferingId, isSaveCardChecked, paymentMethod, accountNumber)
    }
}
