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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.ProcessYourRequestFragment
import za.co.woolworths.financial.services.android.ui.fragments.voc.viewmodel.SurveyProcessRequestVocViewModel
import za.co.woolworths.financial.services.android.util.ConnectionBroadcastReceiver
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class SurveyProcessRequestVocFragment : ProcessYourRequestFragment(), View.OnClickListener {

    companion object {
        const val DURATION_SUCCESS_STATE_IN_MILLISECOND = 5000L
    }

    private var menuItem: MenuItem? = null
    private var navController: NavController? = null
    private val surveyProcessRequestViewModel: SurveyProcessRequestVocViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surveyProcessRequestViewModel.configure(
            details = activity?.intent?.extras?.getSerializable(VoiceOfCustomerActivity.EXTRA_SURVEY_DETAILS) as? SurveyDetails,
            answers = arguments?.getSerializable(VoiceOfCustomerActivity.EXTRA_SURVEY_ANSWERS) as? HashMap<Long, SurveyAnswer>
        )
        configureUI()

        navController = Navigation.findNavController(view)

        circularProgressListener({}, {}) // onSuccess(), onFailure()

        binding.processRequestNavHostFragment.includePMAProcessingFailure.apply {
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
        }

        autoConnection()
    }

    private fun configureUI() {
        (activity as? VoiceOfCustomerActivity)?.apply {
            setToolbarSkipVisibility(show = false)
            hideToolbarBackButton()
        }
        binding.processRequestNavHostFragment.includePMAProcessing.apply {
            processRequestTitleTextView?.text = bindString(R.string.voc_processing_request_title)
            processRequestDescriptionTextView?.text =
                bindString(R.string.voc_processing_request_desc)
        }
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

        binding.processRequestNavHostFragment.apply {
            includePMAProcessingSuccess?.root?.visibility = GONE
            includePMAProcessingFailure?.root?.visibility = GONE
            includePMAProcessing?.root?.visibility = VISIBLE
            includePMAProcessing?.processRequestTitleTextView?.text = bindString(R.string.voc_processing_request_title)
        }

        surveyProcessRequestViewModel.performSubmitSurveyRepliesRequest(
            onSuccess = ::onRequestSuccessful,
            onFailed = ::onRequestFailed
        )
    }

    private fun onRequestSuccessful() {
        viewLifecycleOwner.lifecycleScope.launch {
            stopSpinning(true)
            with(binding.processRequestNavHostFragment.includePMAProcessing) {
                processRequestTitleTextView?.text = bindString(R.string.voc_request_successful_title)
                processRequestDescriptionTextView?.text = ""
            }

            delay(DURATION_SUCCESS_STATE_IN_MILLISECOND)
            (activity as? VoiceOfCustomerActivity)?.finishActivity()
        }
    }

    private fun onRequestFailed() {
        menuItem?.isVisible = true
        stopSpinning(false)

        binding.processRequestNavHostFragment.apply {
            includePMAProcessingSuccess?.root?.visibility = GONE
            includePMAProcessingFailure?.root?.visibility = VISIBLE
            includePMAProcessing?.root?.visibility = GONE

            includePMAProcessingFailure?.processRequestTitleTextView?.text =
                bindString(R.string.voc_request_failed_title)
            includePMAProcessingFailure?.processResultFailureTextView?.text = ""
        }
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
                activity?.apply {
                    finish()
                    overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
