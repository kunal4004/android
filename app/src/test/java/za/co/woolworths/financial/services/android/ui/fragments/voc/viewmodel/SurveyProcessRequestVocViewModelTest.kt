package za.co.woolworths.financial.services.android.ui.fragments.voc.viewmodel

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.models.network.AppContextProviderStub
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.RetrofitApiProviderStub
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

@RunWith(PowerMockRunner::class)
@PrepareForTest(Util::class, Utils::class, FirebaseManager::class, FirebaseApp::class, FirebaseCrashlytics::class)
internal class SurveyProcessRequestVocViewModelTest {

    private lateinit var mockStaticUtil: MockedStatic<Util>
    private lateinit var mockStaticUtils: MockedStatic<Utils>
    private lateinit var mockFirebaseManager: MockedStatic<FirebaseManager>
    private lateinit var mockFirebaseApp: MockedStatic<FirebaseApp>
    private lateinit var mockFirebaseCrashlytics: MockedStatic<FirebaseCrashlytics>
    private lateinit var mockApiService: OneAppServiceTestDouble

    private lateinit var dummyQuestions: ArrayList<SurveyQuestion>
    private lateinit var dummySurvey: SurveyDetails
    private lateinit var SUT: SurveyProcessRequestVocViewModel

    @Before
    fun setUp() {
        mockStaticUtil = mockStatic(Util::class.java)
        mockStaticUtils = mockStatic(Utils::class.java)
        mockFirebaseManager = mockStatic(FirebaseManager::class.java)
        mockFirebaseApp = mockStatic(FirebaseApp::class.java)
        mockFirebaseCrashlytics = mockStatic(FirebaseCrashlytics::class.java)

        `when`(Util.isDebug(any(Context::class.java)))
            .thenReturn(false)
        `when`(Utils.getUniqueDeviceID())
            .thenReturn("")
        `when`(FirebaseApp.initializeApp(any(Context::class.java)))
            .thenReturn(mock(FirebaseApp::class.java))
        `when`(FirebaseCrashlytics.getInstance())
            .thenReturn(mock(FirebaseCrashlytics::class.java))

        mockApiService = OneAppServiceTestDouble()

        dummyQuestions = ArrayList()
        dummyQuestions.add(
            SurveyQuestion(
                id = 1,
                type = "NUMERIC",
                title = "This is an optional numeric question.",
                required = true,
                minValue = 1,
                maxValue = 11,
                matrix = true,
                column = 1,
                group = 2
            )
        )
        dummyQuestions.add(
            SurveyQuestion(
                id = 2,
                type = "FREE_TEXT",
                title = "This is an optional free-text question.",
                required = false,
                matrix = false,
                column = 3,
                group = 4
            )
        )
        dummySurvey = SurveyDetails(
            id = 1,
            name = "Survey to test validation.",
            type = "GENEX",
            questions = dummyQuestions
        )

        SUT = SurveyProcessRequestVocViewModel()
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
    fun configure_SurveyWithAnswers_AdditionalQuestionDataTransferredToAnswers() {
        // Arrange
        val dummyAnswers = HashMap<Long, SurveyAnswer>()
        dummyAnswers[1] = SurveyAnswer(
            questionId = 1,
            answerId = 5
        )
        dummyAnswers[2] = SurveyAnswer(
            questionId = 2,
            textAnswer = "Lorem ipsum"
        )

        // Act
        SUT.configure(dummySurvey, dummyAnswers, mockApiService)

        // Assert
        assertEquals("Matrix value should be transferred from question to answer", SUT.getAnswers()?.get(1)?.matrix, true)
        assertEquals("Column value should be transferred from question to answer", SUT.getAnswers()?.get(1)?.column, 1)
        assertEquals("Group value should be transferred from question to answer", SUT.getAnswers()?.get(1)?.group, 2)
        assertEquals("Matrix value should be transferred from question to answer", SUT.getAnswers()?.get(2)?.matrix, false)
        assertEquals("Column value should be transferred from question to answer", SUT.getAnswers()?.get(2)?.column, 3)
        assertEquals("Group value should be transferred from question to answer", SUT.getAnswers()?.get(2)?.group, 4)
    }

    @Test
    fun configure_SurveyWithAnswers_RateSliderOffsetValueAdded() {
        // Arrange
        val dummyAnswers = HashMap<Long, SurveyAnswer>()
        dummyAnswers[1] = SurveyAnswer(
            questionId = 1,
            answerId = 5
        )

        // Act
        SUT.configure(dummySurvey, dummyAnswers, mockApiService)

        // Assert
        assertEquals("Offset is expected to be added to answerId for Rate Slider answer", SUT.getAnswers()?.get(1)?.answerId, 6)
    }

    @Test
    fun configure_RateSliderRequiredQuestion_AnswerDefaultsToMax() {
        // Arrange
        dummyQuestions.find { it.id == 1L }?.required = true
        val dummyAnswers = HashMap<Long, SurveyAnswer>()
        dummyAnswers[1] = SurveyAnswer(
            questionId = 1
        )

        // Act
        SUT.configure(dummySurvey, dummyAnswers, mockApiService)

        // Assert
        assertEquals("Answer, when not set, is expected to be max for Rate Slider answer", SUT.getAnswers()?.get(1)?.answerId, 11)
    }

    @Test
    fun configure_FreeTextRequiredQuestion_AnswerDefaultsToNA() {
        // Arrange
        dummyQuestions.find { it.id == 2L }?.required = true
        val dummyAnswers = HashMap<Long, SurveyAnswer>()
        dummyAnswers[2] = SurveyAnswer(
            questionId = 2
        )

        // Act
        SUT.configure(dummySurvey, dummyAnswers, mockApiService)

        // Assert
        assertEquals("Answer, when not set, is expected to be a default string for Free Text answer", SUT.getAnswers()?.get(2)?.textAnswer, "N/A")
    }

    @Test
    fun configure_RateSliderOptionalQuestion_AnswerRemovedIfNull() {
        // Arrange
        dummyQuestions.find { it.id == 1L }?.required = false
        val dummyAnswers = HashMap<Long, SurveyAnswer>()
        dummyAnswers[1] = SurveyAnswer(
            questionId = 1
        )

        // Act
        SUT.configure(dummySurvey, dummyAnswers, mockApiService)

        // Assert
        assertNull("Optional Rate Slider answer with no value is expected to be removed", SUT.getAnswers()?.get(1))
    }

    @Test
    fun configure_FreeTextOptionalQuestion_AnswerRemovedIfNull() {
        // Arrange
        dummyQuestions.find { it.id == 2L }?.required = false
        val dummyAnswers = HashMap<Long, SurveyAnswer>()
        dummyAnswers[2] = SurveyAnswer(
            questionId = 2
        )

        // Act
        SUT.configure(dummySurvey, dummyAnswers, mockApiService)

        // Assert
        assertNull("Optional Free Text answer with no value is expected to be removed", SUT.getAnswers()?.get(2))
    }

    @Test
    fun performSubmitSurveyRepliesRequest_NoSurveyDetails_RequestFailed() {
        // Arrange
        val mockSuccessCallback = mock<() -> Unit>()
        val mockFailureCallback = mock<() -> Unit>()
        val dummyAnswers = HashMap<Long, SurveyAnswer>()
        SUT.configure(null, dummyAnswers, mockApiService)

        // Act
        SUT.performSubmitSurveyRepliesRequest(mockSuccessCallback, mockFailureCallback)

        // Assert
        verifyNoInteractions(mockSuccessCallback)
        verify(mockFailureCallback, times(1)).invoke()
    }

    @Test
    fun performSubmitSurveyRepliesRequest_NoAnswer_RequestFailed() {
        // Arrange
        val mockSuccessCallback = mock<() -> Unit>()
        val mockFailureCallback = mock<() -> Unit>()
        SUT.configure(dummySurvey, null, mockApiService)

        // Act
        SUT.performSubmitSurveyRepliesRequest(mockSuccessCallback, mockFailureCallback)

        // Assert
        verifyNoInteractions(mockSuccessCallback)
        verify(mockFailureCallback, times(1)).invoke()
    }

    @Test
    fun performSubmitSurveyRepliesRequest_ApiServiceToSucceed_RequestSuccessful() {
        // Arrange
        val mockSuccessCallback = mock<() -> Unit>()
        val mockFailureCallback = mock<() -> Unit>()
        val dummyAnswers = HashMap<Long, SurveyAnswer>()

        val responseMock = Response.success<Void>(null)
        val callMock = CallVoidTestDouble()
        callMock.isSuccessScenario = true
        callMock.mockSuccessResponse = responseMock
        mockApiService.mockCall = callMock

        SUT.configure(dummySurvey, dummyAnswers, mockApiService)

        // Act
        SUT.performSubmitSurveyRepliesRequest(mockSuccessCallback, mockFailureCallback)

        // Assert
        verify(mockSuccessCallback, times(1)).invoke()
        verifyNoInteractions(mockFailureCallback)
        assertEquals(mockApiService.countSubmitVocSurveyReplies, 1)
        assertEquals(callMock.countEnqueue, 1)
    }

    @Test
    fun performSubmitSurveyRepliesRequest_ApiServiceToSucceedWithFailedResponse_RequestFailed() {
        // Arrange
        val mockSuccessCallback = mock<() -> Unit>()
        val mockFailureCallback = mock<() -> Unit>()
        val dummyAnswers = HashMap<Long, SurveyAnswer>()

        val responseMock = Response.error<Void>(500, mock(ResponseBody::class.java))
        val callMock = CallVoidTestDouble()
        callMock.isSuccessScenario = true
        callMock.mockSuccessResponse = responseMock
        mockApiService.mockCall = callMock

        SUT.configure(dummySurvey, dummyAnswers, mockApiService)

        // Act
        SUT.performSubmitSurveyRepliesRequest(mockSuccessCallback, mockFailureCallback)

        // Assert
        verifyNoInteractions(mockSuccessCallback)
        verify(mockFailureCallback, times(1)).invoke()
        assertEquals(mockApiService.countSubmitVocSurveyReplies, 1)
        assertEquals(callMock.countEnqueue, 1)
    }

    @Test
    fun performSubmitSurveyRepliesRequest_ApiServiceToFail_RequestFailed() {
        // Arrange
        val mockSuccessCallback = mock<() -> Unit>()
        val mockFailureCallback = mock<() -> Unit>()
        val dummyAnswers = HashMap<Long, SurveyAnswer>()

        val exception = RuntimeException("Something went wrong")
        val callMock = CallVoidTestDouble()
        callMock.isSuccessScenario = false
        callMock.mockFailureThrowable = exception
        mockApiService.mockCall = callMock

        val exceptionArgument = ArgumentCaptor.forClass(Throwable::class.java)

        SUT.configure(dummySurvey, dummyAnswers, mockApiService)

        // Act
        SUT.performSubmitSurveyRepliesRequest(mockSuccessCallback, mockFailureCallback)

        // Assert
        verifyNoInteractions(mockSuccessCallback)
        verify(mockFailureCallback, times(1)).invoke()
        assertEquals(mockApiService.countSubmitVocSurveyReplies, 1)
        assertEquals(callMock.countEnqueue, 1)
        // TODO UNIT TEST: Find what's wrong with the below code
//        verify(FirebaseManager::class.java, times(1))
//        FirebaseManager.logException(exceptionArgument.capture())
//        assertEquals(exceptionArgument.value, exception)
    }

    class OneAppServiceTestDouble: OneAppService(AppContextProviderStub(), RetrofitApiProviderStub()) {
        var mockCall: Call<Void>? = null
        var countSubmitVocSurveyReplies = 0

        override fun submitVocSurveyReplies(
            surveyDetails: SurveyDetails,
            surveyAnswers: HashMap<Long, SurveyAnswer>
        ): Call<Void> {
            countSubmitVocSurveyReplies += 1
            return mockCall ?: super.submitVocSurveyReplies(surveyDetails, surveyAnswers)
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

        override fun request(): Request = mock(Request::class.java)

        override fun timeout(): Timeout = mock(Timeout::class.java)
    }
}