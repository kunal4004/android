package za.co.woolworths.financial.services.android.startup.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
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
@RunWith(MockitoJUnitRunner::class)
class StartupViewModelTest : ViewModel() {
    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private lateinit var startUpRepository: StartUpRepository
    private lateinit var startupApiHelper: StartupApiHelper
    private lateinit var apiObserver: Observer<ConfigResource>
    private lateinit var startupViewModel: StartupViewModel
    private lateinit var instrumentationContext: Context

    @Before
    fun setUp() {
        startUpRepository = mock()
        startupApiHelper = mock()
        apiObserver = mock()
        instrumentationContext = mock(Context::class.java, RETURNS_DEEP_STUBS)
        startupViewModel = StartupViewModel(startUpRepository, startupApiHelper)
        TestViewModel()
    }

    @Ignore
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



   @Ignore
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

    @Ignore
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
}