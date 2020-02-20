package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IOTPLinkStoreCard
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.npc.LinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardResponse
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorMessageDialogWithTitleFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.SessionUtilities
import java.net.ConnectException
import java.net.UnknownHostException

class StoreCardOTPRequest(private val activity: Activity?, private val otpMethodType: OTPMethodType) {

    private var storeOTPService: Call<LinkNewCardOTP>? = null
    var linkStoreCardHasFailed = false
    var getCardCallHasFailed = false

    fun make(requestListener: IOTPLinkStoreCard<LinkNewCardOTP>) {
        requestListener.showProgress()
        storeOTPService = OneAppService.getLinkNewCardOTP(otpMethodType)
        storeOTPService?.enqueue(
                CompletionHandler(object : IResponseListener<LinkNewCardOTP> {
                    override fun onSuccess(linkNewCardOTP: LinkNewCardOTP) {
                        with(linkNewCardOTP) {
                            when (this.httpCode) {
                                200 -> {
                                    requestListener.hideProgress()
                                    requestListener.onSuccessHandler(linkNewCardOTP)
                                }
                                440 -> sessionExpired(linkNewCardOTP.response)
                                else -> {
                                    requestListener.hideProgress()
                                    requestListener.onFailureHandler()
                                    val errorTitle = activity?.resources?.getString(R.string.absa_general_error_title)
                                    messageDialog(errorTitle)
                                }
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        activity?.apply {
                            runOnUiThread {
                                requestListener.hideProgress()
                                if (error is ConnectException || error is UnknownHostException) {
                                    ErrorHandlerView(this).showToast()
                                    requestListener.onFailureHandler(error)
                                }
                            }
                        }
                    }

                }, LinkNewCardOTP::class.java))
    }

    private fun sessionExpired(response: Response?) {
        activity?.let { activity ->
            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response?.stsParams
                    ?: "", activity)
        }
    }

    private fun messageDialog(response: String?) {
        (activity as? AppCompatActivity)?.apply {
            val dialog = response?.let { ErrorMessageDialogWithTitleFragment.newInstance(it, true) }
            dialog?.show(supportFragmentManager.beginTransaction(), ErrorMessageDialogWithTitleFragment::class.java.simpleName)
        }
    }

    fun linkStoreCardRequest(requestListener: IOTPLinkStoreCard<LinkNewCardResponse>?, linkStoreCard: LinkStoreCard) {
        requestListener?.showProgress()
        OneAppService.linkStoreCardRequest(linkStoreCard).enqueue(CompletionHandler(object : IResponseListener<LinkNewCardResponse> {
            override fun onSuccess(response: LinkNewCardResponse?) {
                linkStoreCardHasFailed = false
                when (response?.httpCode) {
                    200 -> requestListener?.onSuccessHandler(response)
                    440 -> {
                        requestListener?.hideProgress()
                        sessionExpired(response.response)
                    }
                    else -> {
                        linkStoreCardHasFailed = true
                        // requestListener?.hideProgress()
                        requestListener?.onFailureHandler(response?.response)
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                activity?.runOnUiThread {
                    linkStoreCardHasFailed = true
                    requestListener?.hideProgress()
                    requestListener?.onFailureHandler()
                }
            }

        }, LinkNewCardResponse::
        class.java))
    }

    fun getStoreCards(requestListener: IOTPLinkStoreCard<StoreCardsResponse>?, account: Account): Call<StoreCardsResponse> {
        val getStoreCardsRequest = OneAppService.getStoreCards(StoreCardsRequestBody(account.accountNumber, account.productOfferingId))
        getStoreCardsRequest.enqueue(CompletionHandler(object : IResponseListener<StoreCardsResponse> {
            override fun onSuccess(response: StoreCardsResponse) {
                getCardCallHasFailed = false
                when (response.httpCode) {
                    200 -> requestListener?.onSuccessHandler(response)
                    440 -> sessionExpired(response.response)
                    else -> {
                        getCardCallHasFailed = true
                        requestListener?.onFailureHandler()
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                activity?.runOnUiThread {
                    requestListener?.hideProgress()
                    requestListener?.onFailureHandler()
                    getCardCallHasFailed = true
                }
            }
        }, StoreCardsResponse::class.java))
        return getStoreCardsRequest
    }
}

