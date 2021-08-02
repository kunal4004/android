package za.co.woolworths.financial.services.android.ui.fragments.voc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_survey_voc.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerActivity.Companion.EXTRA_SURVEY_ANSWERS
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerInterface
import za.co.woolworths.financial.services.android.ui.adapters.SurveyQuestionAdapter
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GenericActionOrCancelDialogFragment
import za.co.woolworths.financial.services.android.util.FirebaseManager

class SurveyVocFragment : Fragment(), SurveyAnswerDelegate, GenericActionOrCancelDialogFragment.IActionOrCancel {

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
                        setToolbarSkipVisibility(show = true)
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
        // Array to be updated as new question types are implemented.
        // This is just in case the survey contains question types that have not been implemented yet in this version.
        val allowedQuestionTypes = arrayOf(
                SurveyQuestion.QuestionType.RATE_SLIDER.type,
                SurveyQuestion.QuestionType.FREE_TEXT.type
        )
        return questions.filter { item -> allowedQuestionTypes.contains(item.type) }
    }

    private fun updateSubmitButtonState() {
        surveyQuestionAdapter?.apply {
            notifyItemChanged(itemCount - 1, Unit)
        }
    }

    override fun getRecyclerViewHeight(): Int {
        return rvSurveyQuestions.height
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

    override fun isSurveyAnswersValid(): Boolean {
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

    override fun onInputRateSlider(questionId: Long, value: Int) {
        getAnswer(questionId)?.answerId = value
        updateSubmitButtonState()
    }

    override fun onInputFreeText(questionId: Long, value: String) {
        getAnswer(questionId)?.textAnswer = value
        updateSubmitButtonState()
    }

    override fun onSubmit() {
        navController?.navigate(
                R.id.action_surveyVocFragment_to_surveyProcessRequestVocFragment,
                bundleOf(
                        EXTRA_SURVEY_ANSWERS to surveyAnswers
                )
        )
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
            performOptOutRequest()
            (activity as? VoiceOfCustomerActivity)?.apply {
                finishActivity()
            }
        }
    }

    fun performOptOutRequest() {
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