package za.co.woolworths.financial.services.android.ui.fragments.voc.viewmodel

import android.content.Context
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import retrofit2.Callback
import retrofit2.Response
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.models.network.AppContextProviderStub
import za.co.woolworths.financial.services.android.models.network.CallVoidStub
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.RetrofitApiProviderStub
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

@RunWith(PowerMockRunner::class)
@PrepareForTest(Util::class, Utils::class)
class SurveyVocViewModelTest {

    private lateinit var mockStaticUtil: MockedStatic<Util>
    private lateinit var mockStaticUtils: MockedStatic<Utils>
    private lateinit var mockApiService: OneAppService
    private lateinit var SUT: SurveyVocViewModel

    @Before
    fun setup() {
        mockStaticUtil = mockStatic(Util::class.java)
        mockStaticUtils = mockStatic(Utils::class.java)

        `when`(Util.isDebug(ArgumentMatchers.any(Context::class.java)))
            .thenReturn(false)
        `when`(Utils.getUniqueDeviceID())
            .thenReturn("")

        mockApiService = mock(
            OneAppService::class.java,
            withSettings()
                .useConstructor(
                    AppContextProviderStub(),
                    RetrofitApiProviderStub()
                )
        )

        SUT = SurveyVocViewModel()

        val questions = ArrayList<SurveyQuestion>()
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
        val survey = SurveyDetails(
            id = 1,
            name = "Survey to test validation.",
            type = "GENEX",
            questions = questions
        )
        SUT.configure(survey, mockApiService)
    }

    @After
    fun tearDown() {
        mockStaticUtil.close()
        mockStaticUtils.close()
    }

    @Test
    fun getAllowedQuestions_QuestionsContainUnknownType_UnknownTypeIgnored() {
        // Act
        val result = SUT.getAllowedQuestions().size

        // Assert
        assertEquals("Question with unknown type (unimplemented) is expected not to be present in the list of allowed questions", result, 3)
    }

    @Test
    fun isSurveyAnswersValid_NoAnswersYet_ReturnsFalse() {
        // Act
        val result = SUT.isSurveyAnswersValid()

        // Assert
        assertEquals("Validation is expected to fail on initial state", result, false)
    }

    @Test
    fun isSurveyAnswersValid_HasExpectedAnswers_ReturnsTrue() {
        // Arrange
        SUT.setAnswer(1, 6)
        SUT.setAnswer(2, "Lorem ipsum")

        // Act
        val result = SUT.isSurveyAnswersValid()

        // Assert
        assertEquals("Validation is expected to succeed", result, true)
    }

    @Test
    fun isSurveyAnswersValid_HasInvalidAnswer_ReturnsFalse() {
        // Arrange
        SUT.setAnswer(1, 6)
        SUT.setAnswer(2, "")

        // Act
        val result = SUT.isSurveyAnswersValid()

        // Assert
        assertEquals("Validation is expected to fail after clearing a required field", result, false)
    }

    @Test
    fun getAnswer_DefaultState_NumericAnswerEqualsMaxValue() {
        // Act
        val result = SUT.getAnswer(1)?.answerId

        // Assert
        assertEquals("Numeric answer is expected to be max value by default", result, 11)
    }

    @Test
    fun getAnswer_NumericAnswerUpdated_UpdatedAsExpected() {
        // Arrange
        SUT.setAnswer(1, 6)

        // Act
        val result = SUT.getAnswer(1)?.answerId

        // Assert
        assertEquals("Numeric answer is expected to have changed", result, 6)
    }

    @Test
    fun getAnswer_DefaultState_TextAnswerIsNull() {
        // Act
        val result = SUT.getAnswer(2)?.textAnswer

        // Assert
        Assert.assertNull("Free text answer is expected to be null by default", result)
    }

    @Test
    fun getAnswer_TextAnswerUpdated_UpdatedAsExpected() {
        // Arrange
        SUT.setAnswer(2, "Lorem ipsum")

        // Act
        val result = SUT.getAnswer(2)?.textAnswer

        // Assert
        assertEquals("Free text answer is expected to have changed", result, "Lorem ipsum")
    }

    @Test
    fun performOptOutRequest_ApiServiceToSucceed_RequestSentSuccessfully() {
        // Arrange
        val responseMock = Response.success<Void>(null)
        val callMock = mock(CallVoidStub::class.java)//spy(CallStub<Void>())

        doReturn(callMock).`when`(mockApiService).optOutVocSurvey()
//        `when`(mockApiService.optOutVocSurvey())
//            .thenReturn(callMock)

//        doAnswer {
//            (it.arguments[0] as Callback<Void>).onResponse(callMock, responseMock)
//        }.`when`(callMock.enqueue(any()))
        `when`(callMock.enqueue(any()))
            .then {
                val callback = it.arguments[0] as Callback<Void>
                callback.onResponse(callMock, responseMock)
            }

        // Act
        SUT.performOptOutRequest()

        // Assert
        verify(mockApiService).optOutVocSurvey()
        verify(callMock).enqueue(ArgumentMatchers.any())
    }

    @Test
    fun performOptOutRequest_ApiServiceToFail_RequestFailed() {
        // Arrange
        val exception = RuntimeException("Something went wrong")
        val callMock = CallVoidStub()

        `when`(mockApiService.optOutVocSurvey())
            .thenReturn(callMock)
        `when`(callMock.enqueue(any()))
            .then {
                val callback = it.arguments[0] as Callback<Void>
                callback.onFailure(callMock, exception)
            }

        val exceptionArgument = ArgumentCaptor.forClass(Throwable::class.java)
        PowerMockito.mockStatic(FirebaseManager::class.java)

        // Act
        SUT.performOptOutRequest()

        // Assert
        verify(mockApiService).optOutVocSurvey()
        verify(callMock).enqueue(ArgumentMatchers.any())
        verify(FirebaseManager).logException(exceptionArgument.capture())
        assertEquals(exceptionArgument.value, exception)
    }
}