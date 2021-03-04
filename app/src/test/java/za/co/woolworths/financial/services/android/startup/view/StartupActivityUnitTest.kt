package za.co.woolworths.financial.services.android.startup.view

import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.startup.viewmodel.StartupViewModel
import za.co.woolworths.financial.services.android.utils.mock

/**
 * Created by Kunal Uttarwar on 3/3/21.
 */

@RunWith(MockitoJUnitRunner::class)
class StartupActivityUnitTest {

    private lateinit var startupViewModel: StartupViewModel
    private lateinit var startupActivity: StartupActivity

    @Before
    fun init() {
        startupActivity = mock(StartupActivity::class.java, CALLS_REAL_METHODS)
        startupViewModel = mock()
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
}