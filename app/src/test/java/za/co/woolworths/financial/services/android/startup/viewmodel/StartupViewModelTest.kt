package za.co.woolworths.financial.services.android.startup.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
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
    private lateinit var configResponse: ConfigResponse

    @Before
    fun setUp() {
        configResponse = mock()
        startUpRepository = mock()
        startupApiHelper = mock()
        apiObserver = mock()
        TestViewModel()
    }

    @Test
    fun get_shouldReturn_loading_and_Success() = runBlocking {
        val viewModel = StartupViewModel(startUpRepository, startupApiHelper)
        val mockData: ConfigResponse = mock()
        `when`(startUpRepository.queryServiceGetConfig()).thenReturn(mockData)
        viewModel.queryServiceGetConfig().observeForever(apiObserver)
        testCoroutineRule.postDelay().await()
        verify(apiObserver).onChanged(ConfigResource.loading(null))
        verify(apiObserver).onChanged(ConfigResource.success(mockData))
        verify(startUpRepository, times(1)).queryServiceGetConfig()
        viewModel.queryServiceGetConfig().removeObserver(apiObserver)
    }


    @Test
    fun get_shouldReturnError() = runBlocking {
        val errorMessage = "Error Occurred!"
        `when`(startUpRepository.queryServiceGetConfig()).thenThrow(RuntimeException(errorMessage))
        val viewModel = StartupViewModel(startUpRepository, startupApiHelper)
        viewModel.queryServiceGetConfig().observeForever(apiObserver)
        testCoroutineRule.postDelay().await()
        verify(apiObserver).onChanged(ConfigResource.loading(null))
        verify(apiObserver).onChanged(
                ConfigResource.error(
                        RuntimeException(errorMessage).toString(),
                        null
                )
        )
        verify(startUpRepository).queryServiceGetConfig()
        viewModel.queryServiceGetConfig().removeObserver(apiObserver)
    }
}