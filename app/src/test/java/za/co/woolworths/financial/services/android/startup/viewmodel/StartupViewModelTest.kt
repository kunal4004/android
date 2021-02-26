package za.co.woolworths.financial.services.android.startup.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.test.InstrumentationRegistry
import com.awfs.coordination.BuildConfig
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.IsNull
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.startup.service.repository.StartUpRepository
import za.co.woolworths.financial.services.android.startup.utils.ConfigResource
import za.co.woolworths.financial.services.android.utils.TestCoroutineRule
import za.co.woolworths.financial.services.android.utils.mock

/**
 * Created by Kunal Uttarwar on 25/2/21.
 */

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class StartupViewModelTest : ViewModel() {
    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private lateinit var startUpRepository: StartUpRepository
    private lateinit var startupApiHelper: StartupApiHelper
    private lateinit var apiObserver: Observer<ConfigResource>
    private lateinit var configResponse: ConfigResponse
    private lateinit var startupViewModel: StartupViewModel
    private lateinit var instrumentationContext: Context

    @Before
    fun setUp() {
        configResponse = mock()
        startUpRepository = mock()
        startupApiHelper = mock()
        apiObserver = mock()
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        startupViewModel = StartupViewModel(startUpRepository, startupApiHelper)
        TestViewModel()
    }

    @Test
    fun get_shouldReturn_loading_and_Success() = runBlocking {
        val mockData: ConfigResponse = mock()
        `when`(startUpRepository.queryServiceGetConfig()).thenReturn(mockData)
        startupViewModel.queryServiceGetConfig().observeForever(apiObserver)
        testCoroutineRule.postDelay().await()
        verify(apiObserver).onChanged(ConfigResource.loading(null))
        verify(apiObserver).onChanged(ConfigResource.success(mockData))
        verify(startUpRepository, times(1)).queryServiceGetConfig()
        startupViewModel.queryServiceGetConfig().removeObserver(apiObserver)
    }


    @Test
    fun get_shouldReturnError() = runBlocking {
        val errorMessage = "Error Occurred!"
        `when`(startUpRepository.queryServiceGetConfig()).thenThrow(RuntimeException(errorMessage))
        startupViewModel.queryServiceGetConfig().observeForever(apiObserver)
        testCoroutineRule.postDelay().await()
        verify(apiObserver).onChanged(ConfigResource.loading(null))
        verify(apiObserver).onChanged(
                ConfigResource.error(
                        RuntimeException(errorMessage).toString(),
                        null
                )
        )
        verify(startUpRepository).queryServiceGetConfig()
        startupViewModel.queryServiceGetConfig().removeObserver(apiObserver)
    }

    @Test
    fun check_if_methods_gets_called() {
        startupViewModel.setSessionDao(SessionDao.KEY.SPLASH_VIDEO, "1")
        startupViewModel.getSessionDao(SessionDao.KEY.SPLASH_VIDEO)
        startupViewModel.isConnectedToInternet(instrumentationContext)
        startupViewModel.clearSharedPreference(instrumentationContext)
        verify(startUpRepository, times(1)).setSessionDao(SessionDao.KEY.SPLASH_VIDEO, "1")
        verify(startUpRepository, times(1)).getSessionDao(SessionDao.KEY.SPLASH_VIDEO)
        verify(startupApiHelper, times(1)).isConnectedToInternet(instrumentationContext)
        verify(startUpRepository, times(1)).clearSharedPreference(instrumentationContext)
    }

    @Test
    fun check_for_environment_variable() {
        startupViewModel.setUpEnvironment(instrumentationContext)
        Assert.assertEquals(BuildConfig.ENV, startupViewModel.environment)
        Assert.assertThat(startupViewModel.firebaseAnalytics, `is`(IsNull.notNullValue()))
    }

    @Test
    fun check_for_firebase_events() {
        //FirebaseApp.initializeApp(instrumentationContext)
        startupViewModel.setUpEnvironment(instrumentationContext)
        startupViewModel.setUpFirebaseEvents()
    }
}