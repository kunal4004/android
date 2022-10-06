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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity.Companion.EXTRA_SURVEY_ANSWERS
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerInterface
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.fragments.voc.viewmodel.SurveyVocViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GenericActionOrCancelDialogFragment
import za.co.woolworths.financial.services.android.ui.views.voc.SurveyFooterActionView
import za.co.woolworths.financial.services.android.ui.views.voc.SurveyQuestionFreeTextView
import za.co.woolworths.financial.services.android.ui.views.voc.SurveyQuestionRateSliderView
import za.co.woolworths.financial.services.android.util.Utils

class SurveyVocFragment : Fragment(), GenericActionOrCancelDialogFragment.IActionOrCancel {

    companion object {
        const val DIALOG_OPT_OUT_ID = 2
        const val VIEW_TYPE_FOOTER = 3
    }

    private var navController: NavController? = null
    private val surveyViewModel: SurveyVocViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = NavHostFragment.findNavController(this)
        surveyViewModel.configure(
            details = activity?.intent?.extras?.getSerializable(VoiceOfCustomerActivity.EXTRA_SURVEY_DETAILS) as? SurveyDetails
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView {
        Render()
    }

    @Preview
    @Composable
    fun Render() {
        val questions = rememberSaveable { surveyViewModel.getAllowedQuestions() }
        var isSubmitEnabled by rememberSaveable {
            mutableStateOf(surveyViewModel.isSurveyAnswersValid())
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
                                    var isTooltipInitialPositionUpdated = false
                                    rateSliderView.bind(question, surveyViewModel.getAnswer(question.id)) { questionId, value ->
                                        onInputRateSlider(questionId, value)
                                        // No need to update submit button's state here,
                                        // since slider already has a default value, whether it's required or not
                                    }
                                    AndroidView(
                                        factory = { rateSliderView },
                                        modifier = Modifier.onGloballyPositioned {
                                            if (!isTooltipInitialPositionUpdated) {
                                                isTooltipInitialPositionUpdated = true
                                                rateSliderView.post {
                                                    rateSliderView.updateSliderTooltipPosition()
                                                }
                                            }
                                        }
                                    )
                                }
                                SurveyQuestion.QuestionType.FREE_TEXT -> {
                                    SurveyQuestionFreeTextView(
                                        title = question.title,
                                        initialText = surveyViewModel.getAnswer(question.id)?.textAnswer,
                                        placeholder = if (question.required == true) R.string.voc_question_freetext_hint_required else R.string.voc_question_freetext_hint_optional,
                                        onTextChanged = { value ->
                                            onInputFreeText(question.id, value)
                                            isSubmitEnabled = surveyViewModel.isSurveyAnswersValid()
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

    private fun onInputRateSlider(questionId: Long, value: Int) {
        surveyViewModel.setAnswer(questionId, value)
    }

    private fun onInputFreeText(questionId: Long, value: String) {
        surveyViewModel.setAnswer(questionId, value)
    }

    private fun onSubmit() {
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.VOC_SUBMIT, activity)
        navController?.navigate(
                R.id.action_surveyVocFragment_to_surveyProcessRequestVocFragment,
                bundleOf(
                        EXTRA_SURVEY_ANSWERS to surveyViewModel.getAnswers()
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
            surveyViewModel.performOptOutRequest()
            (activity as? VoiceOfCustomerActivity)?.apply {
                finishActivity()
            }
        }
    }
}