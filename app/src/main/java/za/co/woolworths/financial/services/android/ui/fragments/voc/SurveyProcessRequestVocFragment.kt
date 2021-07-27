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
import kotlinx.android.synthetic.main.fragment_store_confirmation.*
import kotlinx.android.synthetic.main.pma_process_detail_layout.*
import kotlinx.android.synthetic.main.processing_request_failure_fragment.*
import kotlinx.android.synthetic.main.processing_request_failure_fragment.processRequestTitleTextView
import kotlinx.android.synthetic.main.processing_request_failure_fragment.view.*
import kotlinx.android.synthetic.main.processing_request_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity.Companion.DEFAULT_VALUE_RATE_SLIDER_MAX
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
    private var surveyDetails: SurveyDetails? = null
    private var surveyAnswers: HashMap<Long, SurveyAnswer>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureSurveyDetailsAndAnswers()
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

    private fun configureSurveyDetailsAndAnswers() {
        surveyDetails = activity?.intent?.extras?.getSerializable(VoiceOfCustomerActivity.EXTRA_SURVEY_DETAILS) as? SurveyDetails
        arguments?.apply {
            surveyAnswers = getSerializable(VoiceOfCustomerActivity.EXTRA_SURVEY_ANSWERS) as? HashMap<Long, SurveyAnswer>
        }
        surveyDetails?.questions?.forEach { question ->
            // Pass some data required by Genex
            surveyAnswers?.get(question.id)?.apply {
                matrix = question.matrix
                column = question.column
                group = question.group

                if (question.type == SurveyQuestion.QuestionType.RATE_SLIDER.type) {
                    // Add back offset removed to draw slider
                    answerId?.let {
                        answerId = it + 1
                    }
                }

                // Set default answer value for required questions
                if (question.required == true) {
                    when (question.type) {
                        SurveyQuestion.QuestionType.RATE_SLIDER.type -> {
                            if (answerId == null) {
                                answerId = (question.maxValue ?: DEFAULT_VALUE_RATE_SLIDER_MAX) - 1
                            }
                        }
                        SurveyQuestion.QuestionType.FREE_TEXT.type -> {
                            if (textAnswer == null) {
                                textAnswer = "N/A" // TODO VOC: UI must reflect this validation
                            }
                        }
                    }
                }
            }
        }
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

        if (surveyDetails == null || surveyAnswers == null) {
            onRequestFailed()
            return
        }

        val submitVocSurveyRepliesRequest = OneAppService.submitVocSurveyReplies(surveyDetails!!, surveyAnswers!!)
        submitVocSurveyRepliesRequest.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onRequestSuccessful()
                } else {
                    onRequestFailed()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onRequestFailed()
            }
        })
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
