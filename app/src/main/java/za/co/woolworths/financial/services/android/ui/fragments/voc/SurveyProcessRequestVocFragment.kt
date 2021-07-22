package za.co.woolworths.financial.services.android.ui.fragments.voc

import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pma_process_detail_layout.*
import kotlinx.android.synthetic.main.processing_request_failure_fragment.*
import kotlinx.android.synthetic.main.processing_request_failure_fragment.processRequestTitleTextView
import kotlinx.android.synthetic.main.processing_request_failure_fragment.view.*
import kotlinx.android.synthetic.main.processing_request_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.ProcessYourRequestFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class SurveyProcessRequestVocFragment : ProcessYourRequestFragment(), View.OnClickListener {

    companion object {
        const val DURATION_SUCCESS_STATE_IN_MILLISECOND = 5000L
    }

    private var menuItem: MenuItem? = null
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureUI()

        navController = Navigation.findNavController(view)

        circularProgressListener({}, {}) // onSuccess(), onFailure()

        btnRetryProcessPayment?.apply {
            setOnClickListener(this@SurveyProcessRequestVocFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        callCenterNumberTextView?.apply {
            visibility = VISIBLE
            text = bindString(R.string.cancel)
            setOnClickListener(this@SurveyProcessRequestVocFragment)
            AnimationUtilExtension.animateViewPushDown(this)
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        autoConnection()
    }

    private fun configureUI() {
        (activity as? VoiceOfCustomerActivity)?.apply {
            setToolbarSkipVisibility(show = false)
            hideToolbarBackButton()
        }
        processRequestTitleTextView?.text = bindString(R.string.voc_processing_request_title)
        processRequestDescriptionTextView?.text = bindString(R.string.voc_processing_request_desc)
    }

    private fun autoConnection() {
        activity?.let { activity ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(activity, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    when (hasConnection) {
                        true -> performSurveyAnswerRequest()
                        else -> return
                    }
                }
            })
        }
    }

    private fun performSurveyAnswerRequest() {
        menuItem?.isVisible = false
        startSpinning()
        includePMAProcessingSuccess?.visibility = GONE
        includePMAProcessingFailure?.visibility = GONE
        includePMAProcessing?.visibility = VISIBLE
        processRequestTitleTextView?.text = bindString(R.string.voc_processing_request_title)

        Handler(getMainLooper()).postDelayed({
            onRequestSuccessful()
//            onRequestFailed()
        }, 3000)
    }

    private fun onRequestSuccessful() {
        stopSpinning(true)
        processRequestTitleTextView?.text = bindString(R.string.voc_request_successful_title)
        processRequestDescriptionTextView?.text = ""

        Handler(getMainLooper()).postDelayed({
            (activity as? VoiceOfCustomerActivity)?.apply {
                finishActivity()
            }
        }, DURATION_SUCCESS_STATE_IN_MILLISECOND)
    }

    private fun onRequestFailed() {
        menuItem?.isVisible = true
        stopSpinning(false)

        includePMAProcessingSuccess?.visibility = GONE
        includePMAProcessingFailure?.visibility = VISIBLE
        includePMAProcessing?.visibility = GONE

        includePMAProcessingFailure?.processRequestTitleTextView?.text = bindString(R.string.voc_request_failed_title)
        includePMAProcessingFailure?.processResultFailureTextView?.text = ""
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnRetryProcessPayment -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    performSurveyAnswerRequest()
                } else {
                    ErrorHandlerView(activity).showToast()
                }
            }
            R.id.callCenterNumberTextView -> { // used as a cancel button
                (activity as? VoiceOfCustomerActivity)?.apply {
                    finishActivity()
                }
            }
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
                activity?.finish()
                activity?.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
