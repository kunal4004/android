package za.co.woolworths.financial.services.android.ui.fragments.voc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity.Companion.EXTRA_SURVEY_ANSWERS
import za.co.woolworths.financial.services.android.ui.adapters.SurveyQuestionAdapter
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.views.voc.SurveyQuestionFreeTextView
import za.co.woolworths.financial.services.android.ui.views.voc.SurveyQuestionRateSliderView
import za.co.woolworths.financial.services.android.ui.views.voc.SurveyFooterActionView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GenericActionOrCancelDialogFragment
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

class SurveyVocFragment : Fragment(), GenericActionOrCancelDialogFragment.IActionOrCancel { //, SurveyAnswerDelegate {

    companion object {
        const val DIALOG_OPT_OUT_ID = 2
    }

    private var navController: NavController? = null
    private var surveyQuestionAdapter: SurveyQuestionAdapter? = null
    private var surveyDetails: SurveyDetails? = null
    private val surveyAnswers = HashMap<Long, SurveyAnswer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = NavHostFragment.findNavController(this)
        surveyDetails = activity?.intent?.extras?.getSerializable(VoiceOfCustomerActivity.EXTRA_SURVEY_DETAILS) as? SurveyDetails
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView {
        // TODO: move to ViewModel, example: https://medium.com/mobile-app-development-publication/managing-compose-state-variable-with-and-without-viewmodel-8da72abef1e
        val questions = rememberSaveable { surveyDetails?.questions ?: ArrayList() }
        var isSubmitEnabled by rememberSaveable {
            mutableStateOf(isSurveyAnswersValid())
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                count = questions.size + 1,
                contentType = { index ->
                    if (index < questions.size) {
                        SurveyQuestion.QuestionType.ofType(questions[index].type)?.viewType
                            ?: SurveyQuestion.QuestionType.FREE_TEXT.viewType
                    }
                    SurveyQuestionAdapter.VIEW_TYPE_FOOTER
                },
                itemContent = { index ->
                    if (index < questions.size) {
                        val question = questions[index]
                        when (SurveyQuestion.QuestionType.ofType(question.type)) {
                            SurveyQuestion.QuestionType.RATE_SLIDER -> {
                                // Using an Android View inside of a Compose View
                                val rateSliderView = SurveyQuestionRateSliderView(LocalContext.current)
                                rateSliderView.bind(question, getAnswer(question.id)) { questionId, value ->
                                    onInputRateSlider(questionId, value)
                                    isSubmitEnabled = isSurveyAnswersValid()
                                }
                                AndroidView(factory = { rateSliderView })
                            }
                            SurveyQuestion.QuestionType.FREE_TEXT -> {
                                // Using an Android View inside of a Compose View
                                val freeTextView = SurveyQuestionFreeTextView(LocalContext.current)
                                freeTextView.bind(question, getAnswer(question.id)) { questionId, value ->
                                    onInputFreeText(questionId, value)
                                    isSubmitEnabled = isSurveyAnswersValid()
                                }
                                AndroidView(factory = { freeTextView })
                            }
                            else -> {}
                        }
                    } else {
                        // Footer - Submit or Skip
                        SurveyFooterActionView(
                            isSubmitEnabled = isSubmitEnabled,
                            onSubmitCallback = ::onSubmit,
                            onOptOutCallback = ::onOptOut
                        )
                    }
                }
            )
        }
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.fragment_survey_voc, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        setupToolbar()
//        if (surveyQuestionAdapter == null) {
//            initRecyclerView()
//        }
//    }
//
//    private fun setupToolbar() {
//        activity?.apply {
//            when (this) {
//                is VoiceOfCustomerInterface -> {
//                    context?.let {
//                        setToolbarSkipVisibility(show = true)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun initRecyclerView() {
//        if (surveyDetails == null || surveyDetails!!.questions.isNullOrEmpty()) return
//        context?.let {
//            rvSurveyQuestions.layoutManager = LinearLayoutManager(it, RecyclerView.VERTICAL, false)
//            surveyQuestionAdapter = SurveyQuestionAdapter(it, getAllowedQuestions(surveyDetails!!.questions!!), this)
//        }
//        rvSurveyQuestions.adapter = surveyQuestionAdapter
//        addOnRecyclerViewHeightChangeListener()
//    }
//
//    fun addOnRecyclerViewHeightChangeListener() {
//        rvSurveyQuestions.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
//            // Update footer spacing if RecyclerView's height changed, when keyboard is shown/hidden for example
//            if ((bottom - top) != (oldBottom - oldTop)) {
//                updateSubmitButtonStateAndSpacing()
//            }
//        }
//        // Update spacing on first load
//        updateSubmitButtonStateAndSpacing()
//    }
//
//    private fun getAllowedQuestions(questions: ArrayList<SurveyQuestion>): List<SurveyQuestion> {
//        // Array to be updated as new question types are implemented.
//        // This is just in case the survey contains question types that have not been implemented yet in this version.
//        val allowedQuestionTypes = arrayOf(
//                SurveyQuestion.QuestionType.RATE_SLIDER.type,
//                SurveyQuestion.QuestionType.FREE_TEXT.type
//        )
//        return questions.filter { item -> allowedQuestionTypes.contains(item.type) }
//    }

    private fun updateSubmitButtonStateAndSpacing() {
        // TODO
//        surveyQuestionAdapter?.apply {
//            notifyItemChanged(itemCount - 1, Unit)
//        }
    }

//    override fun getRecyclerViewHeight(): Int {
//        return rvSurveyQuestions.height
//    }

    private fun getAnswer(questionId: Long): SurveyAnswer? {
        var answer = surveyAnswers[questionId]
        if (answer == null) {
            val question = surveyDetails!!.questions!!.first { it.id == questionId }
            // Set default answer
            answer = when (question.type) {
                SurveyQuestion.QuestionType.RATE_SLIDER.type -> {
                    SurveyAnswer(
                            questionId = question.id,
                            answerId = question.maxValue
                    )
                }
                else -> {
                    SurveyAnswer(
                            questionId = question.id
                    )
                }
            }
            surveyAnswers[questionId] = answer
        }
        return answer
    }

    fun isSurveyAnswersValid(): Boolean {
        val questions = surveyDetails?.questions ?: run { return false }
        for (question: SurveyQuestion in questions) {
            if (question.required == true) {
                val answer = getAnswer(question.id) ?: run { return false }
                when (question.type) {
                    SurveyQuestion.QuestionType.RATE_SLIDER.type -> {
                        if (answer.answerId == null) return false
                    }
                    else -> {
                        if (answer.textAnswer.isNullOrBlank()) return false
                    }
                }
            }
        }
        return true
    }

    private fun onInputRateSlider(questionId: Long, value: Int) {
        // No need to update submit button's state here,
        // since slider already has a default value, whether it's required or not
        getAnswer(questionId)?.answerId = value
    }

    private fun onInputFreeText(questionId: Long, value: String) {
        getAnswer(questionId)?.textAnswer = value
        updateSubmitButtonStateAndSpacing()
    }

    private fun onSubmit() {
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.VOC_SUBMIT, activity)
        navController?.navigate(
                R.id.action_surveyVocFragment_to_surveyProcessRequestVocFragment,
                bundleOf(
                        EXTRA_SURVEY_ANSWERS to surveyAnswers
                )
        )
    }

    private fun onOptOut() {
        activity?.let {
            val dialog = GenericActionOrCancelDialogFragment.newInstance(
                    dialogId = DIALOG_OPT_OUT_ID,
                    title = getString(R.string.voc_opt_out_dialog_title),
                    desc = getString(R.string.voc_opt_out_dialog_desc),
                    actionButtonText = getString(R.string.voc_opt_out_dialog_action),
                    cancelButtonText = getString(R.string.voc_opt_out_dialog_cancel),
                    this
            )
            dialog.show(it.supportFragmentManager, GenericActionOrCancelDialogFragment::class.java.simpleName)
        }
    }

    override fun onDialogActionClicked(dialogId: Int) {
        if (dialogId == DIALOG_OPT_OUT_ID) {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.VOC_OPTOUT, activity)
            performOptOutRequest()
            (activity as? VoiceOfCustomerActivity)?.apply {
                finishActivity()
            }
        }
    }

    private fun performOptOutRequest() {
        val optOutVocSurveyRequest = OneAppService.optOutVocSurvey()
        optOutVocSurveyRequest.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // Response not needed
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Ignored if request fails
                FirebaseManager.logException(t)
            }
        })
    }
}