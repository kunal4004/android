package za.co.woolworths.financial.services.android.startup.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.startup.utils.ConfigResource
import za.co.woolworths.financial.services.android.startup.viewmodel.StartupViewModel
import za.co.woolworths.financial.services.android.utils.TestCoroutineRule
import za.co.woolworths.financial.services.android.utils.mock

/**
 * Created by Kunal Uttarwar on 3/3/21.
 */

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StartupActivityUnitTest : Activity() {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var apiObserver: Observer<ConfigResource>
    private lateinit var startupViewModel: StartupViewModel
    private lateinit var startupActivity: StartupActivity

    @Before
    fun init() {
        startupActivity = mock(StartupActivity::class.java, CALLS_REAL_METHODS)
        startupViewModel = mock()
        apiObserver = mock()
        startupActivity.testSetViewModelInstance(startupViewModel)
    }

    @Test
    fun check_all_methods_from_init() = runBlockingTest {
        doNothing().`when`(startupActivity).showNonVideoViewWithErrorLayout()
        startupActivity.init()
        verify(startupViewModel, times(1)).setSessionDao(SessionDao.KEY.SPLASH_VIDEO, "1")
        verify(startupActivity, times(1)).showNonVideoViewWithErrorLayout()
        verify(startupViewModel, times(1)).isConnectedToInternet(startupActivity)
        verify(startupViewModel, times(1)).clearSharedPreference(startupActivity)
    }

    @Test
    fun check_firebase_methods_from_init() = runBlockingTest {
        `when`(startupViewModel.isConnectedToInternet(startupActivity)).thenReturn(true)
        startupActivity.init()
        verify(startupViewModel, times(1)).setUpFirebaseEvents()
    }

    @Test
    fun showsVideoView_for_first_time() {
        `when`(startupViewModel.getSessionDao(SessionDao.KEY.SPLASH_VIDEO)).thenReturn(true)
        doNothing().`when`(startupActivity).showVideoView()
        startupActivity.testsetupLoadingScreen()
        verify(startupActivity, times(1)).showVideoView()
    }

    @Test
    fun showsNonVideoView_for_second_time() {
        doNothing().`when`(startupActivity).showNonVideoViewWithoutErrorLayout()
        startupActivity.testsetupLoadingScreen()
        verify(startupActivity, times(1)).showNonVideoViewWithoutErrorLayout()
    }

    @Test
    fun testPresentServerMessage() {
        `when`(startupViewModel.isSplashScreenDisplay).thenReturn(true)
        doNothing().`when`(startupActivity).showServerMessage()
        startupActivity.presentNextScreenOrServerMessage()
        verify(startupActivity, times(1)).showServerMessage()
        Assert.assertTrue(startupViewModel.isSplashScreenDisplay)
    }

    @Test
    fun testPresentNextScreen() {
        `when`(startupViewModel.isSplashScreenDisplay).thenReturn(false)
        doNothing().`when`(startupActivity).showNonVideoViewWithoutErrorLayout()
        val intent: Intent = mock()
        val bundle: Bundle = mock()
        intent.putExtras(bundle)
        startupActivity.testSetIntent(intent)

        `when`(intent.action).thenReturn("android.intent.action.VIEW")
        `when`(intent.data).thenReturn(mock())
        doNothing().`when`(startupActivity).handleAppLink(any())

        startupActivity.presentNextScreenOrServerMessage()
        verify(startupActivity, times(1)).showNonVideoViewWithoutErrorLayout()
        verify(startupActivity, times(1)).presentNextScreen()
        verify(startupActivity, times(1)).handleAppLink(any())
        Assert.assertFalse(startupViewModel.isSplashScreenDisplay)
    }

    @Test
    fun testPresentNextScreen_if_appLink_is_null() {
        val intent: Intent = mock()
        val bundle: Bundle = mock()
        intent.putExtras(bundle)
        startupActivity.testSetIntent(intent)

        `when`(intent.action).thenReturn("android.intent.action.VIEW")
        `when`(intent.data).thenReturn(null)
        `when`(intent.extras).thenReturn(mock())
        doNothing().`when`(startupActivity).handleAppLink(any())

        startupActivity.presentNextScreen()
        verify(startupActivity, times(1)).handleAppLink(any())
    }

    @Test
    fun testonStartInit() {
        `when`(startupViewModel.isAppMinimized).thenReturn(true)
        `when`(startupViewModel.isServerMessageShown).thenReturn(true)
        doNothing().`when`(startupActivity).showNonVideoViewWithoutErrorLayout()
        doNothing().`when`(startupActivity).getConfig()
        startupActivity.onStartInit()
        verify(startupActivity, times(1)).getConfig()
        verify(startupActivity, times(1)).showNonVideoViewWithoutErrorLayout()
        Assert.assertTrue(startupViewModel.isServerMessageShown)
        Assert.assertTrue(startupViewModel.isAppMinimized)
    }

    @Test
    fun testonStartInit_if_is_not_AppMinimized() {
        `when`(startupViewModel.isAppMinimized).thenReturn(false)
        doNothing().`when`(startupActivity).getConfig()
        `when`(startupViewModel.isConnectedToInternet(startupActivity)).thenReturn(true)
        startupActivity.onStartInit()
        verify(startupViewModel, times(1)).isConnectedToInternet(startupActivity)
        Assert.assertFalse(startupViewModel.isAppMinimized)
    }

    @Test
    fun checkIfConfig_returns_response() = runBlocking {
        val mockData: LiveData<ConfigResource> = mock()
        `when`(startupViewModel.queryServiceGetConfig()).thenReturn(mockData)
        startupViewModel.queryServiceGetConfig().observeForever(apiObserver)
        startupActivity.getConfig()
        testCoroutineRule.postDelay().await()
        /*verify(apiObserver).onChanged(ConfigResource.loading(null))
        verify(apiObserver).onChanged(ConfigResource.success(any()))
        verify(startupViewModel, times(1)).queryServiceGetConfig()*/
        startupViewModel.queryServiceGetConfig().removeObserver(apiObserver)
    }
}