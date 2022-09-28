package za.co.woolworths.financial.services.android.ui.fragments.voc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
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
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerInterface
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GenericActionOrCancelDialogFragment
import za.co.woolworths.financial.services.android.ui.views.voc.SurveyFooterActionView
import za.co.woolworths.financial.services.android.ui.views.voc.SurveyQuestionFreeTextView
import za.co.woolworths.financial.services.android.ui.views.voc.SurveyQuestionRateSliderView
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

class SurveyVocFragment : Fragment(), GenericActionOrCancelDialogFragment.IActionOrCancel {

    companion object {
        const val DIALOG_OPT_OUT_ID = 2
        const val VIEW_TYPE_FOOTER = 3
    }

    private var navController: NavController? = null
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
        val questions = rememberSaveable { getAllowedQuestions(surveyDetails?.questions ?: ArrayList()) }
        var isSubmitEnabled by rememberSaveable {
            mutableStateOf(isSurveyAnswersValid())
        }
        var positionLazyColumnBottom by remember { mutableStateOf(0f) }
        var positionBottomSpacer by remember { mutableStateOf(Offset.Zero) }
        var positionContainerBottomSpacer by remember { mutableStateOf(Offset.Zero) }
        val density = LocalDensity.current

        Column {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = true)
                    .background(colorResource(id = R.color.color_separator_lighter_grey))
                    .onGloballyPositioned { coordinates ->
                        if (positionLazyColumnBottom <= 0f) {
                            positionLazyColumnBottom = coordinates.positionInWindow().y + coordinates.size.height.toFloat()
                        }
                    }
            ) {
                items(
                    count = questions.size + 1,
                    contentType = { index ->
                        if (index < questions.size) {
                            SurveyQuestion.QuestionType.ofType(questions[index].type)?.viewType
                                ?: SurveyQuestion.QuestionType.FREE_TEXT.viewType
                        }
                        VIEW_TYPE_FOOTER
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
                                        // No need to update submit button's state here,
                                        // since slider already has a default value, whether it's required or not
                                    }
                                    AndroidView(factory = { rateSliderView })
                                }
                                SurveyQuestion.QuestionType.FREE_TEXT -> {
                                    SurveyQuestionFreeTextView(
                                        title = question.title,
                                        initialText = getAnswer(question.id)?.textAnswer,
                                        placeholder = if (question.required == true) R.string.voc_question_freetext_hint_required else R.string.voc_question_freetext_hint_optional,
                                        onTextChanged = { value ->
                                            onInputFreeText(question.id, value)
                                            isSubmitEnabled = isSurveyAnswersValid()
                                        }
                                    )
                                }
                                else -> {}
                            }
                            Spacer(
                                modifier = Modifier
                                    .height(8.dp)
                            )
                        } else {
                            if (
                                positionBottomSpacer != Offset.Zero &&
                                positionContainerBottomSpacer != Offset.Zero &&
                                positionContainerBottomSpacer.y - positionBottomSpacer.y > 0
                            ) {
                                density.run {
                                    Spacer(
                                        Modifier.height((positionContainerBottomSpacer.y - positionBottomSpacer.y).toDp())
                                    )
                                }
                            }
                            SurveyFooterActionView(
                                isSubmitEnabled = isSubmitEnabled,
                                onSubmitCallback = ::onSubmit,
                                onOptOutCallback = ::onOptOut
                            )
                            Spacer(
                                Modifier
                                    .background(Color.Red)
                                    .height(0.dp)
                                    .onGloballyPositioned { coordinates ->
                                        if (positionBottomSpacer == Offset.Zero) {
                                            positionBottomSpacer = coordinates.positionInWindow()
                                        }
                                    }
                            )
                        }
                    }
                )
            }
            Spacer(
                Modifier
                    .background(Color.Red)
                    .height(0.dp)
                    .onGloballyPositioned { coordinates ->
                        positionContainerBottomSpacer = coordinates.positionInWindow()
                    }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    private fun setupToolbar() {
        activity?.apply {
            when (this) {
                is VoiceOfCustomerInterface -> {
                    context?.let {
                        setToolbarSkipVisibility(show = true)
                    }
                }
            }
        }
    }

    private fun getAllowedQuestions(questions: ArrayList<SurveyQuestion>): List<SurveyQuestion> {
        // Array to be updated as new question types are implemented.
        // This is just in case the survey contains question types that have not been implemented yet in this version.
        val allowedQuestionTypes = arrayOf(
                SurveyQuestion.QuestionType.RATE_SLIDER.type,
                SurveyQuestion.QuestionType.FREE_TEXT.type
        )
        return questions.filter { item -> allowedQuestionTypes.contains(item.type) }
    }

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

    private fun isSurveyAnswersValid(): Boolean {
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
        getAnswer(questionId)?.answerId = value
    }

    private fun onInputFreeText(questionId: Long, value: String) {
        getAnswer(questionId)?.textAnswer = value
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