package za.co.woolworths.financial.services.android.util.voc

import android.content.Context
import android.content.Intent
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.Request
import okio.Timeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigCustomerFeedback
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.models.network.AppContextProviderStub
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.RetrofitApiProviderStub
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent
import za.co.woolworths.financial.services.android.models.dto.Response as DtoResponse

@RunWith(PowerMockRunner::class)
@PrepareForTest(Util::class, Utils::class, FirebaseManager::class, FirebaseApp::class, FirebaseCrashlytics::class)
internal class VoiceOfCustomerManagerTest {

    private lateinit var mockStaticUtil: MockedStatic<Util>
    private lateinit var mockStaticUtils: MockedStatic<Utils>
    private lateinit var mockFirebaseManager: MockedStatic<FirebaseManager>
    private lateinit var mockFirebaseApp: MockedStatic<FirebaseApp>
    private lateinit var mockFirebaseCrashlytics: MockedStatic<FirebaseCrashlytics>
    private lateinit var mockApiService: OneAppServiceTestDouble
    private lateinit var mockContext: Context

    private lateinit var SUT: VoiceOfCustomerManager

    @Before
    fun setUp() {
        mockStaticUtil = mockStatic(Util::class.java)
        mockStaticUtils = mockStatic(Utils::class.java)
        mockFirebaseManager = mockStatic(FirebaseManager::class.java)
        mockFirebaseApp = mockStatic(FirebaseApp::class.java)
        mockFirebaseCrashlytics = mockStatic(FirebaseCrashlytics::class.java)
        mockContext = mock(Context::class.java, RETURNS_DEEP_STUBS)

        `when`(Util.isDebug(any(Context::class.java)))
            .thenReturn(false)
        `when`(Utils.getUniqueDeviceID())
            .thenReturn("")
        `when`(FirebaseApp.initializeApp(any(Context::class.java)))
            .thenReturn(mock(FirebaseApp::class.java))
        `when`(FirebaseCrashlytics.getInstance())
            .thenReturn(mock(FirebaseCrashlytics::class.java))

        VoiceOfCustomerManager.pendingTriggerEvent = null
        mockApiService = OneAppServiceTestDouble()
        SUT = spy(VoiceOfCustomerManager(mockApiService))
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
    fun showPendingSurveyIfNeeded_NoPendingEvent_NoAction(){
        // Arrange
        VoiceOfCustomerManager.pendingTriggerEvent = null

        // Act
        SUT.showPendingSurveyIfNeeded(mockContext)

        // Assert
        verify(SUT, never()).showVocSurveyIfNeeded(any(), any())
    }

    @Test
    fun showPendingSurveyIfNeeded_HasPendingEvent_ShowPendingSurveyAndClear() {
        // Arrange
        VoiceOfCustomerManager.pendingTriggerEvent = VocTriggerEvent.CHAT_CC_MYACCOUNTS

        // Act
        SUT.showPendingSurveyIfNeeded(mockContext)

        // Assert
        verify(SUT, times(1)).showVocSurveyIfNeeded(any(), any())
        assertNull("Pending trigger event is expected to be null after showPendingSurveyIfNeeded method is called", VoiceOfCustomerManager.pendingTriggerEvent)
    }

    @Test
    fun showVocSurveyIfNeeded_NullTriggerEvent_NoAction() {
        // Act
        SUT.showVocSurveyIfNeeded(mockContext, null)

        // Assert
        verify(SUT, never()).showVocSurvey(anyOrNull(), anyOrNull())
    }

    @Test
    fun showVocSurveyIfNeeded_AppVersionNotSupported_NoAction() {
        // Arrange
        `when`(Utils.isFeatureEnabled(any()))
            .thenReturn(false)

        // Act
        SUT.showVocSurveyIfNeeded(mockContext, VocTriggerEvent.CHAT_CC_MYACCOUNTS)

        // Assert
        verify(SUT, never()).showVocSurvey(anyOrNull(), anyOrNull())
    }

    @Test
    fun showVocSurveyIfNeeded_TriggerEventNotAllowed_NoAction() {
        // Arrange
        `when`(Utils.isFeatureEnabled(any()))
            .thenReturn(true)

        AppConfigSingleton.customerFeedback = ConfigCustomerFeedback(
            0,
            emptyList()
        )

        // Act
        SUT.showVocSurveyIfNeeded(mockContext, VocTriggerEvent.CHAT_CC_MYACCOUNTS)

        // Assert
        verify(SUT, never()).showVocSurvey(anyOrNull(), anyOrNull())
    }

    @Test
    fun showVocSurveyIfNeeded_ApiServiceToSucceed_ShowSurveyAction() {
        // Arrange
        `when`(Utils.isFeatureEnabled(any()))
            .thenReturn(true)

        AppConfigSingleton.customerFeedback = ConfigCustomerFeedback(
            0,
            listOf(VocTriggerEvent.CHAT_CC_MYACCOUNTS.value)
        )

        val responseMock = Response.success<SurveyDetailsResponse>(
            SurveyDetailsResponse(
                survey = mock(SurveyDetails::class.java),
                response = mock(DtoResponse::class.java),
                httpCode = "200"
            )
        )
        val callMock = CallSurveyDetailsTestDouble()
        callMock.isSuccessScenario = true
        callMock.mockSuccessResponse = responseMock
        mockApiService.mockCall = callMock

        // Act
        SUT.showVocSurveyIfNeeded(mockContext, VocTriggerEvent.CHAT_CC_MYACCOUNTS)

        // Assert
        assertEquals(mockApiService.countGetVocSurvey, 1)
        assertEquals(callMock.countEnqueue, 1)
        verify(SUT, times(1)).showVocSurvey(anyOrNull(), anyOrNull())
    }

    @Test
    fun showVocSurveyIfNeeded_ApiServiceToFail_NoAction() {
        // Arrange
        `when`(Utils.isFeatureEnabled(any()))
            .thenReturn(true)

        AppConfigSingleton.customerFeedback = ConfigCustomerFeedback(
            0,
            listOf(VocTriggerEvent.CHAT_CC_MYACCOUNTS.value)
        )

        val exception = RuntimeException("Something went wrong")
        val callMock = CallSurveyDetailsTestDouble()
        callMock.isSuccessScenario = false
        callMock.mockFailureThrowable = exception
        mockApiService.mockCall = callMock

        // Act
        SUT.showVocSurveyIfNeeded(mockContext, VocTriggerEvent.CHAT_CC_MYACCOUNTS)

        // Assert
        assertEquals(mockApiService.countGetVocSurvey, 1)
        assertEquals(callMock.countEnqueue, 1)
        verify(SUT, never()).showVocSurvey(anyOrNull(), anyOrNull())
    }

    @Test
    fun showVocSurvey_InvalidSurvey_NoAction() {
        // Arrange
        val dummySurvey = SurveyDetails(
            id = 0,
            type = "",
            questions = ArrayList()
        )

        // Act
        SUT.showVocSurvey(mockContext, dummySurvey)

        verify(mockContext, never()).startActivity(any(Intent::class.java))
    }

    @Test
    fun showVocSurvey_ValidSurvey_ActivityStarted() {
        // Arrange
        val dummySurvey = SurveyDetails(
            id = 0,
            type = "",
            questions = ArrayList(
                listOf(
                    mock(SurveyQuestion::class.java)
                )
            )
        )

        // Act
        SUT.showVocSurvey(mockContext, dummySurvey)

        verify(mockContext, times(1)).startActivity(any(Intent::class.java))
    }

    class OneAppServiceTestDouble: OneAppService(AppContextProviderStub(), RetrofitApiProviderStub()) {
        var mockCall: Call<SurveyDetailsResponse>? = null
        var countGetVocSurvey = 0

        override fun getVocSurvey(triggerEvent: VocTriggerEvent): Call<SurveyDetailsResponse> {
            countGetVocSurvey += 1
            return mockCall ?: super.getVocSurvey(triggerEvent)
        }
    }

    class CallSurveyDetailsTestDouble: Call<SurveyDetailsResponse> {
        var isSuccessScenario = true
        var mockSuccessResponse: Response<SurveyDetailsResponse>? = null
        var mockFailureThrowable: Throwable? = null
        var countEnqueue = 0

        override fun clone(): Call<SurveyDetailsResponse> = CallSurveyDetailsTestDouble()

        override fun execute(): Response<SurveyDetailsResponse> = Response.success(null)

        override fun enqueue(callback: Callback<SurveyDetailsResponse>) {
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