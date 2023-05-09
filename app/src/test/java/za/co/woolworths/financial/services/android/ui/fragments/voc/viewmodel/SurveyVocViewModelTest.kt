package za.co.woolworths.financial.services.android.ui.fragments.voc.viewmodel

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.Request
import okio.Timeout
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
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.models.network.AppContextProviderStub
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.RetrofitApiProviderStub
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

@RunWith(PowerMockRunner::class)
@PrepareForTest(Util::class, Utils::class, FirebaseManager::class, FirebaseApp::class, FirebaseCrashlytics::class)
internal class SurveyVocViewModelTest {

    private lateinit var mockStaticUtil: MockedStatic<Util>
    private lateinit var mockStaticUtils: MockedStatic<Utils>
    private lateinit var mockFirebaseManager: MockedStatic<FirebaseManager>
    private lateinit var mockFirebaseApp: MockedStatic<FirebaseApp>
    private lateinit var mockFirebaseCrashlytics: MockedStatic<FirebaseCrashlytics>
    private lateinit var mockApiService: OneAppServiceTestDouble
    private lateinit var SUT: SurveyVocViewModel

    @Before
    fun setUp() {
        mockStaticUtil = mockStatic(Util::class.java)
        mockStaticUtils = mockStatic(Utils::class.java)
        mockFirebaseManager = mockStatic(FirebaseManager::class.java)
        mockFirebaseApp = mockStatic(FirebaseApp::class.java)
        mockFirebaseCrashlytics = mockStatic(FirebaseCrashlytics::class.java)

        `when`(Util.isDebug(ArgumentMatchers.any(Context::class.java)))
            .thenReturn(false)
        `when`(Utils.getUniqueDeviceID())
            .thenReturn("")
        `when`(FirebaseApp.initializeApp(ArgumentMatchers.any(Context::class.java)))
            .thenReturn(mock(FirebaseApp::class.java))
        `when`(FirebaseCrashlytics.getInstance())
            .thenReturn(mock(FirebaseCrashlytics::class.java))

        mockApiService = OneAppServiceTestDouble()

        val dummyQuestions = ArrayList<SurveyQuestion>()
        dummyQuestions.add(
            SurveyQuestion(
                id = 1,
                type = "NUMERIC",
                title = "This is an optional numeric question.",
                required = false,
                minValue = 1,
                maxValue = 11
            )
        )
        dummyQuestions.add(
            SurveyQuestion(
                id = 2,
                type = "FREE_TEXT",
                title = "This is an optional free-text question.",
                required = false
            )
        )
        dummyQuestions.add(
            SurveyQuestion(
                id = 3,
                type = "FREE_TEXT",
                title = "This is a required free-text question.",
                required = true
            )
        )
        dummyQuestions.add(
            SurveyQuestion(
                id = 4,
                type = "UNKNOWN_TYPE",
                title = "This is an optional question with an unknown type, to make sure it is ignored on builds not having such implementation yet.",
                required = false
            )
        )
        val dummySurvey = SurveyDetails(
            id = 1,
            name = "Survey to test validation.",
            type = "GENEX",
            questions = dummyQuestions
        )

        SUT = SurveyVocViewModel()
        SUT.configure(dummySurvey, mockApiService)
    }

    @After
    fun tearDown() {
        mockStaticUtil.close()
        mockStaticUtils.close()
        mockFirebaseManager.close()
        mockFirebaseApp.close()
        mockFirebaseCrashlytics.close()
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
        SUT.setAnswer(3, "Lorem ipsum")

        // Act
        val result = SUT.isSurveyAnswersValid()

        // Assert
        assertEquals("Validation is expected to succeed", result, true)
    }

    @Test
    fun isSurveyAnswersValid_HasInvalidAnswer_ReturnsFalse() {
        // Arrange
        SUT.setAnswer(1, 6)
        SUT.setAnswer(3, "")

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
    fun getAnswers_HasExpectedAnswers_ReturnsListOfAnswers() {
        // Arrange
        val answer1 = 6
        val answer2 = "Lorem ipsum"
        val answer3 = "Sit dolor it"
        SUT.setAnswer(1, answer1)
        SUT.setAnswer(2, answer2)
        SUT.setAnswer(3, answer3)

        // Act
        val result = SUT.getAnswers()

        // Assert
        assertEquals("Correct answer value is expected", result[1]?.answerId, answer1)
        assertEquals("Correct answer value is expected", result[2]?.textAnswer, answer2)
        assertEquals("Correct answer value is expected", result[3]?.textAnswer, answer3)
    }

    @Test
    fun performOptOutRequest_ApiServiceToSucceed_RequestSentSuccessfully() {
        // Arrange
        val responseMock = Response.success<Void>(null)
        val callMock = CallVoidTestDouble()
        callMock.isSuccessScenario = true
        callMock.mockSuccessResponse = responseMock
        mockApiService.mockCall = callMock

        // Act
        SUT.performOptOutRequest()

        // Assert
        assertEquals(mockApiService.countOptOutVocSurvey, 1)
        assertEquals(callMock.countEnqueue, 1)
    }

    @Test
    fun performOptOutRequest_ApiServiceToFail_RequestFailed() {
        // Arrange
        val exception = RuntimeException("Something went wrong")
        val callMock = CallVoidTestDouble()
        callMock.isSuccessScenario = false
        callMock.mockFailureThrowable = exception
        mockApiService.mockCall = callMock

        // Act
        SUT.performOptOutRequest()

        // Assert
        assertEquals(mockApiService.countOptOutVocSurvey, 1)
        assertEquals(callMock.countEnqueue, 1)
    }

    class OneAppServiceTestDouble: OneAppService(AppContextProviderStub(), RetrofitApiProviderStub()) {
        var mockCall: Call<Void>? = null
        var countOptOutVocSurvey = 0

        override fun optOutVocSurvey(): Call<Void> {
            countOptOutVocSurvey += 1
            return mockCall ?: super.optOutVocSurvey()
        }
    }

    class CallVoidTestDouble: Call<Void> {
        var isSuccessScenario = true
        var mockSuccessResponse: Response<Void>? = null
        var mockFailureThrowable: Throwable? = null
        var countEnqueue = 0

        override fun clone(): Call<Void> = CallVoidTestDouble()

        override fun execute(): Response<Void> = Response.success(null)

        override fun enqueue(callback: Callback<Void>) {
            countEnqueue += 1
            if (isSuccessScenario) {
                callback.onResponse(this, mockSuccessResponse)
            } else {
                callback.onFailure(this, mockFailureThrowable)
            }
        }

        override fun isExecuted(): Boolean = false

        override fun cancel() {}

        override fun isCanceled(): Boolean = false

        override fun request(): Request = mock(Request::class.java, RETURNS_MOCKS)

        override fun timeout(): Timeout = mock(Timeout::class.java)
    }
}