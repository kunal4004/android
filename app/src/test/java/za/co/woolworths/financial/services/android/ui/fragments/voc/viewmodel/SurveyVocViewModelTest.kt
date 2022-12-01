package za.co.woolworths.financial.services.android.ui.fragments.voc.viewmodel

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion

class SurveyVocViewModelTest {

    private lateinit var viewModel: SurveyVocViewModel

    @Before
    fun init() {
        viewModel = SurveyVocViewModel()
        var questions = ArrayList<SurveyQuestion>()
        questions.add(
            SurveyQuestion(
                id = 1,
                type = "NUMERIC",
                title = "This is an optional numeric question.",
                required = false,
                minValue = 1,
                maxValue = 11
            )
        )
        questions.add(
            SurveyQuestion(
                id = 2,
                type = "FREE_TEXT",
                title = "This is an optional free-text question.",
                required = false
            )
        )
        questions.add(
            SurveyQuestion(
                id = 2,
                type = "FREE_TEXT",
                title = "This is a required free-text question.",
                required = true
            )
        )
        questions.add(
            SurveyQuestion(
                id = 3,
                type = "UNKNOWN_TYPE",
                title = "This is an optional question with an unknown type, to make sure it is ignored on builds not having such implementation yet.",
                required = false
            )
        )
        var survey = SurveyDetails(
            id = 1,
            name = "Survey to test validation.",
            type = "GENEX",
            questions = questions
        )
        viewModel.configure(survey)
    }

    @Test
    fun surveyVocViewModel_GetAllowedQuestions_UnknownTypeIgnored() {
        Assert.assertEquals("Question with unknown type (unimplemented) is expected not to be present in the list of allowed questions", viewModel.getAllowedQuestions().size, 3)
    }

    @Test
    fun surveyVocViewModel_ValidateSurvey_SurveyUnanswered() {
        Assert.assertEquals("Validation is expected to fail on initial state", viewModel.isSurveyAnswersValid(), false)
    }

    @Test
    fun surveyVocViewModel_ValidateSurvey_ValidAnswers() {
        viewModel.setAnswer(1, 6)
        viewModel.setAnswer(2, "Lorem ipsum")
        Assert.assertEquals("Validation is expected to succeed", viewModel.isSurveyAnswersValid(), true)
        viewModel.setAnswer(2, "")
        Assert.assertEquals("Validation is expected to fail after clearing a required field", viewModel.isSurveyAnswersValid(), false)
    }

    @Test
    fun surveyVocViewModel_UpdateAnswer_ValidAnswersRetrieved() {
        Assert.assertEquals("Numeric answer is expected to be max value by default", viewModel.getAnswer(1)?.answerId, 11)
        viewModel.setAnswer(1, 6)
        Assert.assertEquals("Numeric answer is expected to have changed", viewModel.getAnswer(1)?.answerId, 6)

        Assert.assertNull("Free text answer is expected to be null by default", viewModel.getAnswer(2)?.textAnswer)
        viewModel.setAnswer(2, "Lorem ipsum")
        Assert.assertEquals("Free text answer is expected to have changed", viewModel.getAnswer(2)?.textAnswer, "Lorem ipsum")

    }
}