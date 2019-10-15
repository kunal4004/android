package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IOTPLinkStoreCard
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.npc.LinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardResponse
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.SessionUtilities
import java.net.ConnectException
import java.net.UnknownHostException

class StoreCardOTPRequest(private val activity: Activity?, private val otpMethodType: OTPMethodType) {

    private var storeOTPService: Call<LinkNewCardOTP>? = null

    fun make(requestListener: IOTPLinkStoreCard<LinkNewCardOTP>) {
        requestListener.startLoading()
        storeOTPService = OneAppService.getLinkNewCardOTP(otpMethodType)
        storeOTPService?.enqueue(
                CompletionHandler(object : RequestListener<LinkNewCardOTP> {
                    override fun onSuccess(linkNewCardOTP: LinkNewCardOTP) {
                        with(linkNewCardOTP) {
                            when (this.httpCode) {
                                200 -> {
                                    requestListener.onSuccessHandler(linkNewCardOTP)
                                    requestListener.loadComplete() }
                                440 -> sessionExpired(linkNewCardOTP.response)
                                else -> {
                                    requestListener.loadComplete()
                                    requestListener.onFailureHandler()
                                    messageDialog(linkNewCardOTP.response)
                                }
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        activity?.apply {
                            runOnUiThread {
                                requestListener.loadComplete()
                                if (error is ConnectException || error is UnknownHostException) {
                                    ErrorHandlerView(this).showToast()
                                }
                            }
                        }
                    }

                }, LinkNewCardOTP::class.java))
    }

    private fun sessionExpired(response: Response?) {
        activity?.let { activity ->
            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response?.stsParams ?: "", activity)
        }
    }

    private fun messageDialog(response: Response?) {
        response?.desc?.let { desc ->
            val dialog = ErrorDialogFragment.newInstance(desc)
            (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, ErrorDialogFragment::class.java.simpleName) }
        }
    }

    fun make(requestListener: IOTPLinkStoreCard<LinkNewCardResponse>?, linkStoreCard: LinkStoreCard) {
        requestListener?.startLoading()
        OneAppService.linkStoreCard(linkStoreCard).enqueue(CompletionHandler(object : RequestListener<LinkNewCardResponse> {
            override fun onSuccess(response: LinkNewCardResponse?) {
                requestListener?.loadComplete()
                when (response?.httpCode) {
                    200 -> requestListener?.onSuccessHandler(response)
                    440 -> sessionExpired(response.response)
                    else -> requestListener?.onFailureHandler()
                }
            }

            override fun onFailure(error: Throwable?) {
                activity?.runOnUiThread {
                    requestListener?.loadComplete()
                    requestListener?.onFailureHandler()
                }
            }

        }, LinkNewCardResponse::class.java))
    }
}

