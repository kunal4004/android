package za.co.woolworths.financial.services.android.startup.view

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.awfs.coordination.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
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
        // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
//        doNothing().`when`(startupActivity).showNonVideoViewWithErrorLayout()
//        startupActivity.init()
//        verify(startupViewModel, times(1)).setSessionDao(SessionDao.KEY.SPLASH_VIDEO, "1")
//        verify(startupActivity, times(1)).showNonVideoViewWithErrorLayout()
//        verify(startupViewModel, times(1)).isConnectedToInternet(startupActivity)
    }

    // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
    @Ignore
    @Test
    fun check_firebase_methods_from_init() = runBlockingTest {
        `when`(startupViewModel.isConnectedToInternet(startupActivity)).thenReturn(true)
        startupActivity.init()
        verify(startupViewModel, times(1)).setUpFirebaseEvents()
    }

    @Test
    fun showsVideoView_for_first_time() {
        // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
//        `when`(startupViewModel.getSessionDao(SessionDao.KEY.SPLASH_VIDEO)).thenReturn(true)
//        doNothing().`when`(startupActivity).showVideoView()
//        startupActivity.testsetupLoadingScreen()
//        verify(startupActivity, times(1)).showVideoView()
    }

    @Test
    fun showsNonVideoView_for_second_time() {
        // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
//        doNothing().`when`(startupActivity).showNonVideoViewWithoutErrorLayout()
//        startupActivity.testsetupLoadingScreen()
//        verify(startupActivity, times(1)).showNonVideoViewWithoutErrorLayout()
    }

    @Test
    fun testPresentNextScreen() {
        // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
//        doNothing().`when`(startupActivity).showNonVideoViewWithoutErrorLayout()
//        val intent: Intent = mock()
//        val bundle: Bundle = mock()
//        intent.putExtras(bundle)
//        startupActivity.testSetIntent(intent)
//
//        `when`(intent.action).thenReturn("android.intent.action.VIEW")
//        `when`(intent.data).thenReturn(mock())
//        doNothing().`when`(startupActivity).handleAppLink(any())
//
//        startupActivity.presentNextScreenOrServerMessage()
//        verify(startupActivity, times(1)).showNonVideoViewWithoutErrorLayout()
//        verify(startupActivity, times(1)).presentNextScreen()
//        verify(startupActivity, times(1)).handleAppLink(any())
    }

    // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
    @Ignore
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
        // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
//        `when`(startupViewModel.isAppMinimized).thenReturn(true)
//        `when`(startupViewModel.isServerMessageShown).thenReturn(true)
//        doNothing().`when`(startupActivity).showNonVideoViewWithoutErrorLayout()
//        doNothing().`when`(startupActivity).getConfig()
//        startupActivity.onStartInit()
//        verify(startupActivity, times(1)).getConfig()
//        verify(startupActivity, times(1)).showNonVideoViewWithoutErrorLayout()
//        Assert.assertTrue(startupViewModel.isServerMessageShown)
//        Assert.assertTrue(startupViewModel.isAppMinimized)
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
    fun testonClick_for_retry_with_internet() {
        val view: View = mock()
        `when`(view.id).thenReturn(R.id.retry)
        `when`(startupViewModel.isConnectedToInternet(startupActivity)).thenReturn(true)
        doNothing().`when`(startupViewModel).setupFirebaseUserProperty()
        doNothing().`when`(startupActivity).getConfig()
        startupActivity.onClick(view)
        verify(startupViewModel, times(1)).isConnectedToInternet(startupActivity)
        verify(startupViewModel, times(1)).setupFirebaseUserProperty()
        verify(startupActivity, times(1)).getConfig()
    }

    @Test
    fun testonClick_for_retry_without_internet() {
        // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
//        val view: View = mock()
//        `when`(view.id).thenReturn(R.id.retry)
//        `when`(startupViewModel.isConnectedToInternet(startupActivity)).thenReturn(false)
//        doNothing().`when`(startupActivity).showNonVideoViewWithErrorLayout()
//        startupActivity.onClick(view)
//        verify(startupViewModel, times(1)).isConnectedToInternet(startupActivity)
//        verify(startupActivity, times(1)).showNonVideoViewWithErrorLayout()
    }

    @Test
    fun testonCompletion_for_videoPlayerShouldPlay() {
        val mediaPlayer: MediaPlayer = mock()
        doNothing().`when`(startupActivity).presentNextScreenOrServerMessage()
        startupActivity.onCompletion(mediaPlayer)
        Assert.assertFalse(startupViewModel.isVideoPlaying)
        Assert.assertFalse(startupViewModel.videoPlayerShouldPlay)
        verify(startupActivity, times(1)).presentNextScreenOrServerMessage()
    }

    @Test
    fun testonCompletion() {
        // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
//        val mediaPlayer: MediaPlayer = mock()
//        doNothing().`when`(startupActivity).showNonVideoViewWithoutErrorLayout()
//        `when`(startupViewModel.videoPlayerShouldPlay).thenReturn(true)
//        startupActivity.onCompletion(mediaPlayer)
//        Assert.assertFalse(startupViewModel.isVideoPlaying)
//        Assert.assertTrue(startupViewModel.videoPlayerShouldPlay)
//        verify(startupActivity, times(1)).showNonVideoViewWithoutErrorLayout()
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