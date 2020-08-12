package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import za.co.absa.openbankingapi.woolworths.integration.dto.PayUResponse
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.AddCardResponse
import za.co.woolworths.financial.services.android.models.dto.PayUPayResultRequest
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.util.ConnectionBroadcastReceiver
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import java.net.ConnectException

class PMA3DSecureResultProcessFragment : ProcessYourRequestFragment() {

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
        startSpinning()
        val payUResultRequest = PayUPayResultRequest("", "", "", "")
        request(OneAppService.queryServicePaymentResult(payUResultRequest), object : IGenericAPILoaderView<Any> {

            override fun onSuccess(response: Any?) {
                if (!isAdded) return
                stopSpinning(true)
                hasPMAPostPayUPayCompleted = true
                (response as? PayUResponse)?.apply {
                    when (httpCode) {
                        200 -> {
                            val direction = PMAProcessRequestFragmentDirections.actionPMAProcessRequestFragmentToSecure3DPMAFragment(redirection)
                            navController?.navigate(direction)
                        }
                        440 -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response.stsParams, activity)
                        500 -> {
                            if (response.response.code.startsWith("P0"))
                                navController?.navigate(PMAProcessRequestFragmentDirections.actionPMAProcessRequestFragmentToProcessPaymentFailureFragment())
                            else
                                showError(response)
                        }
                        else -> {
                            showError(response)
                        }
                    }
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
}
