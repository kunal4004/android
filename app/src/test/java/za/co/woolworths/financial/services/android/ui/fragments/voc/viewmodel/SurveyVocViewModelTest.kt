package za.co.woolworths.financial.services.android.ui.fragments.voc.viewmodel

import org.junit.Assert
import org.junit.Before
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion

class SurveyVocViewModelTest {

    private val viewModel = SurveyVocViewModel()

    @Before
    fun init() {
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
        var survey = SurveyDetails(
            id = 1,
            name = "Survey to test validation.",
            type = "GENEX",
            questions = questions
        )
        viewModel.configure(survey)
    }

    fun surveyVocViewModel_ValidateSurvey_SurveyUnanswered() {
        Assert.assertEquals("Validation is expected to fail on initial state", viewModel.isSurveyAnswersValid(), true)
    }

    fun surveyVocViewModel_ValidateSurvey_ValidAnswers() {

    }

    //    func testSurveyValidationAndSubmit() throws {
    //        if let data = try? MockDataHelper.getData(fromJSON: "voc_survey_details_required_questions"),
    //           let response = try? JSONDecoder().decode(SurveyDetailsResponse.self, from: data),
    //           let survey = response.survey,
    //           let allowedQuestions = SurveyVocViewModel.getAllowedQuestions(from: survey) {
    //            let viewModel = SurveyVocViewModel(
    //                service: mockApiService,
    //                details: survey,
    //                questions: allowedQuestions
    //            )
    //
    //            // Initial state of survey
    //            XCTAssertFalse(viewModel.isSurveyRepliesValid(), "Validation is expected to fail on initial state")
    //
    //            // Before and after updating a required question
    //            XCTAssertNil(viewModel.getAnswer(for: 4).textAnswer, "Answer is expected to be nil at this point")
    //            viewModel.updateAnswer(for: 4, with: "Lorem Ipsum")
    //            XCTAssertTrue(viewModel.isSurveyRepliesValid(), "Validation is expected to succeed at this point")
    //            XCTAssertEqual(viewModel.getAnswer(for: 4).textAnswer, "Lorem Ipsum", "Answer is expected to match")
    //
    //            // Simulate clearing the answer for a required question
    //            viewModel.updateAnswer(for: 4, with: "")
    //            XCTAssertFalse(viewModel.isSurveyRepliesValid(), "Validation is expected to fail at this point")
    //
    //            // Validate numeric question's value change
    //            viewModel.updateAnswer(for: 2, with: 6)
    //            XCTAssertEqual(viewModel.getAnswer(for: 2).answerId, 6, "Numeric question's answer is expected to have changed at this point")
    //
    //            XCTAssertFalse(viewModel.isSurveyRepliesValid(), "Validation is expected to succeed at this point")
    //
    //            let expectation = expectation(description: "Submit survey mock request is expected to succeed")
    //            viewModel.performSubmitSurveyRequest {
    //                expectation.fulfill()
    //            } onFailure: {
    //                XCTFail("Submit mock survey is expected to succeed")
    //            }
    //
    //            waitForExpectations(timeout: 1) { error in
    //                if let error = error {
    //                    XCTFail("waitForExpectationsWithTimeout error: \(error)")
    //                }
    //            }
    //        } else {
    //            XCTFail("Mocked survey data is not valid")
    //        }
    //    }
}