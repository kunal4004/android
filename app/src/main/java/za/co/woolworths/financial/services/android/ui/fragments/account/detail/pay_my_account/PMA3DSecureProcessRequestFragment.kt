package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.circle_progress_layout.*
import kotlinx.android.synthetic.main.pma_process_detail_layout.*
import kotlinx.android.synthetic.main.process_payment_success_fragment.*
import kotlinx.android.synthetic.main.processing_request_failure_fragment.*
import kotlinx.android.synthetic.main.processing_request_fragment.processingLayoutTitle
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import java.net.ConnectException

class PMA3DSecureProcessRequestFragment : ProcessYourRequestFragment(), View.OnClickListener {

    private var menuItem: MenuItem? = null
    private var navController: NavController? = null
    private var hasPMAPostPayUPayCompleted: Boolean = false

    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var callUsNumber: String? = "0861 50 20 20"

    companion object {
        const val PMA_TRANSACTION_COMPLETED_RESULT_CODE = 4470
        const val PMA_UPDATE_CARD_RESULT_CODE = 4471
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        navController = Navigation.findNavController(view)

        success_tick?.colorCode = R.color.success_tick_color

        circularProgressListener({}, {}) // onSuccess(), onFailure()

        btnRetryProcessPayment?.apply {
            setOnClickListener(this@PMA3DSecureProcessRequestFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        callCenterNumberTextView?.apply {
            setOnClickListener(this@PMA3DSecureProcessRequestFragment)
            AnimationUtilExtension.animateViewPushDown(this)
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        backToMyAccountButton?.apply {
            setOnClickListener(this@PMA3DSecureProcessRequestFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        autoConnection()
    }

    private fun updateUIOnFailure() {
        menuItem?.isVisible = true
        val failureMessage = bindString(R.string.unable_to_process_your_request_desc)
        processResultFailureTextView?.text = failureMessage
        callUsNumber = failureMessage.replace("[^0-9]".toRegex(), "")
        includePMAProcessingSuccess?.visibility = GONE
        includePMAProcessingFailure?.visibility = VISIBLE
        includePMAProcessing?.visibility = GONE

    }

    private fun updateUIOnFailure(desc: String?) {
        menuItem?.isVisible = true
        desc?.apply {
            // keep numeric characters only
            val number = this.replace("[^0-9]".toRegex(), "")
            if (number.isNotEmpty()) {
                callCenterNumberTextView?.visibility = VISIBLE
                callUsNumber = number
            } else {
                callUsNumber = "0861 50 20 20"
                callCenterNumberTextView?.visibility = GONE
            }
        }
        stopSpinning(false)
        processResultFailureTextView?.text = desc
        includePMAProcessingSuccess?.visibility = GONE
        includePMAProcessingFailure?.visibility = VISIBLE
        includePMAProcessing?.visibility = GONE
    }

    private fun showPMAProcess() {
        menuItem?.isVisible = false
        includePMAProcessingSuccess?.visibility = GONE
        includePMAProcessingFailure?.visibility = GONE
        includePMAProcessing?.visibility = VISIBLE
    }

    private fun autoConnection() {
        activity?.let { activity ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(activity, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    when (hasConnection && !hasPMAPostPayUPayCompleted) {
                        true -> postUPayResult()
                        else -> return
                    }
                }
            })
        }
    }

    private fun setupToolbar() {
        (activity as? PayMyAccountActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            showPMAProcess()
            configureToolbar("")
            displayToolbarDivider(false)
        }
    }

    private fun postUPayResult() {
        processingLayoutTitle?.text = bindString(R.string.processing_your_payment_label)
        startSpinning()
        val payUPayResultRequest = payMyAccountViewModel.getPayUPayResultRequest()
        request(payUPayResultRequest?.let { pay -> OneAppService.queryServicePaymentResult(pay) }, object : IGenericAPILoaderView<Any> {

            override fun onSuccess(response: Any?) {
                if (!isAdded) return
                isAPICallSuccessFul = true
                hasPMAPostPayUPayCompleted = true
                (response as? PayUPayResultResponse)?.apply {
                    when (httpCode) {
                        200 -> {
                            if (paymentSuccessful) {
                                stopSpinning(true)
                                paymentValueTextView?.text = Utils.removeNegativeSymbol(WFormatter.newAmountFormat(amount))
                                updateUIOnSuccess()
                            } else {
                                stopSpinning(false)
                                updateUIOnFailure()
                            }
                        }
                        440 -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response.stsParams, activity)
                        502 -> {
                            stopSpinning(false)
                            if (response.response.code.startsWith("P0"))
                                response.response.desc?.let { desc -> updateUIOnFailure(desc) }
                            else
                                response.response.desc?.let { desc -> updateUIOnFailure(desc) }
                        }
                        else -> {
                            response.response.desc?.let { desc -> updateUIOnFailure(desc) }
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

    private fun updateUIOnSuccess() {
        menuItem?.isVisible = true
        includePMAProcessingSuccess?.visibility = VISIBLE
        includePMAProcessing?.visibility = GONE
        includePMAProcessingFailure?.visibility = GONE
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnRetryProcessPayment -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    showPMAProcess()
                    postUPayResult()
                } else {
                    ErrorHandlerView(activity).showToast()
                }
            }
            R.id.callCenterNumberTextView -> {
                Utils.makeCall(callUsNumber)
            }

            R.id.backToMyAccountButton -> {
                finishActivity()
            }
        }
    }

    private fun finishActivity() {
        payMyAccountViewModel.triggerFirebaseEventForPaymentComplete()
        activity?.apply {
            setResult(PMA_TRANSACTION_COMPLETED_RESULT_CODE, Intent().putExtra(PayMyAccountActivity.PAYMENT_DETAIL_CARD_UPDATE, payMyAccountViewModel.getCardDetailInStringFormat()))
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.close_menu_item, menu)
        menuItem = menu.findItem(R.id.closeIcon)
        menuItem?.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.closeIcon -> {
                finishActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
