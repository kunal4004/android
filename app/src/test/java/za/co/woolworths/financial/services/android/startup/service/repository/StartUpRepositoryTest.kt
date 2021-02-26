package za.co.woolworths.financial.services.android.startup.service.repository

import android.content.Context
import androidx.test.InstrumentationRegistry
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.utils.mock

/**
 * Created by Kunal Uttarwar on 26/2/21.
 */

@RunWith(RobolectricTestRunner::class)
class StartUpRepositoryTest {

    private lateinit var startUpRepository: StartUpRepository
    private lateinit var startupApiHelper: StartupApiHelper
    private lateinit var instrumentationContext: Context

    @Before
    fun init() {
        startupApiHelper = mock()
        startUpRepository = StartUpRepository(startupApiHelper)
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun check_if_methods_gets_called() = runBlockingTest {
        startUpRepository.queryServiceGetConfig()
        startUpRepository.clearSharedPreference(instrumentationContext)
        startUpRepository.setSessionDao(SessionDao.KEY.SPLASH_VIDEO, "1")
        Assert.assertTrue(startUpRepository.getSessionDao(SessionDao.KEY.SPLASH_VIDEO))

        verify(startupApiHelper, times(1)).getConfig()
        //verify(sessionDaoSave(SessionDao.KEY.SPLASH_VIDEO, "1"))
        //verify(clearSharedPreferences(instrumentationContext))
        //verify(getSessionDaoValue(SessionDao.KEY.SPLASH_VIDEO))
    }
}