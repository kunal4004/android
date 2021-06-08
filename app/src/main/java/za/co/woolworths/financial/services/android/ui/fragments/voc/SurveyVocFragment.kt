package za.co.woolworths.financial.services.android.ui.fragments.voc

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_survey_voc.*
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerInterface
import za.co.woolworths.financial.services.android.ui.adapters.SurveyQuestionAdapter
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GenericActionOrCancelDialogFragment

class SurveyVocFragment : Fragment(), SurveyAnswerDelegate, GenericActionOrCancelDialogFragment.IActionOrCancel {

    companion object {
        const val SURVEY_DETAILS = "surveyDetails"
        const val DIALOG_OPT_OUT_ID = 1
    }

    private var surveyQuestionAdapter: SurveyQuestionAdapter? = null
    private var surveyDetails: SurveyDetails? = null
    private val surveyAnswers = HashMap<Long, SurveyAnswer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getSerializable(SURVEY_DETAILS)?.let { survey ->
            if (survey is SurveyDetails) {
                surveyDetails = survey
            }
        }

        // TODO: remove dummy code
        val dummyQuestions = ArrayList<SurveyQuestion>()
        for (i in 1..10) {
            dummyQuestions.add(SurveyQuestion(
                    id = i.toLong(),
                    type = "NUMERIC",
                    title = "$i. Please rate how satisfied you are with the LiveChat experience?",
                    minValue = 1,
                    maxValue = 10,
                    required = true,
                    matrix = false
            ))
        }
        for (i in 11..20) {
            dummyQuestions.add(SurveyQuestion(
                    id = i.toLong(),
                    type = "FREE_TEXT",
                    title = "$i. Please tell us how the LiveChat service could make more of a difference to you",
                    required = false,
                    matrix = false
            ))
        }
        surveyDetails = SurveyDetails(
                id = 1,
                name = "Live Chat",
                type = "GENEX",
                questions = dummyQuestions
        )

        // TODO: generate default answers for required questions... unless not necessary
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_survey_voc, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        if (surveyQuestionAdapter == null) {
            initRecyclerView()
        }
    }

    private fun setupToolbar() {
        activity?.apply {
            when (this) {
                is VoiceOfCustomerInterface -> {
                    context?.let {
                        // TODO: remove and replace with Not Now button to the right
                        setToolbarTitle("Survey")
                        setToolbarTitleGravity(Gravity.CENTER_HORIZONTAL)
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        if (surveyDetails == null || surveyDetails!!.questions.isNullOrEmpty()) return
        context?.let {
            rvSurveyQuestions.layoutManager = LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            surveyQuestionAdapter = SurveyQuestionAdapter(it, getAllowedQuestions(surveyDetails!!.questions!!), this)
        }
        rvSurveyQuestions.adapter = surveyQuestionAdapter
    }

    private fun getAllowedQuestions(questions: ArrayList<SurveyQuestion>): List<SurveyQuestion> {
        // Array to be updated as new types are implemented.
        // This is just in case the survey contains question types that have not been implemented yet in this version.
        val allowedQuestionTypes = arrayOf(
                SurveyQuestion.QuestionType.RATE_SLIDER.type,
                SurveyQuestion.QuestionType.FREE_TEXT.type
        )
        return questions.filter { item -> allowedQuestionTypes.contains(item.type)}
    }

    override fun getAnswer(questionId: Long): SurveyAnswer? {
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

    override fun onInputRateSlider(questionId: Long, value: Int) {
        getAnswer(questionId)?.answerId = value
    }

    override fun onInputFreeText(questionId: Long, value: String) {
        getAnswer(questionId)?.textAnswer = value
    }

    override fun onSubmit() {
        Toast.makeText(context, "Submit Tapped", Toast.LENGTH_SHORT).show()
    }

    override fun onOptOut() {
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
            Toast.makeText(context, "Opt Out Confirmation Tapped", Toast.LENGTH_SHORT).show()
        }
    }
}