package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IStoreCardOTPCallback
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.views.ProductListingProgressBar
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.SessionUtilities
import java.io.IOException

class OTPRequest(private val activity: Activity?, private val otpMethodType: OTPMethodType) {

    private var storeOTPService: Call<LinkNewCardOTP>? = null
    private val mPbOTP: ProductListingProgressBar? = ProductListingProgressBar()

    fun make(requestListener: IStoreCardOTPCallback<LinkNewCardOTP>) {
        showProgressBar()
        storeOTPService = OneAppService.getLinkNewCardOTP(otpMethodType)
        storeOTPService?.enqueue(
                CompletionHandler(object : RequestListener<LinkNewCardOTP> {
                    override fun onSuccess(linkNewCardOTP: LinkNewCardOTP) {
                        hideProgressBar()
                        with(linkNewCardOTP) {
                            when (this.httpCode) {
                                200 -> requestListener.onSuccess(linkNewCardOTP)

                                440 -> activity?.let { activity ->
                                    SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE,
                                            response?.stsParams ?: "", activity)
                                }

                                else -> response?.desc?.let { desc ->
                                    val dialog = ErrorDialogFragment.newInstance(desc)
                                    (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, ErrorDialogFragment::class.java.simpleName) }
                                }
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        activity?.apply {
                            runOnUiThread {
                                hideProgressBar()
                                if (error is IOException) {
                                    ErrorHandlerView(this).showToast()
                                }
                            }
                        }
                    }

                }, LinkNewCardOTP::class.java))
    }

    private fun showProgressBar() = activity?.let { activity -> mPbOTP?.show(activity) }

    private fun hideProgressBar() = mPbOTP?.dialog?.dismiss()
}

