package za.co.woolworths.financial.services.android.startup.service.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.powermock.modules.junit4.PowerMockRunner
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.utils.mock
import za.co.woolworths.financial.services.android.utils.setFinalStatic


/**
 * Created by Kunal Uttarwar on 26/2/21.
 */

@RunWith(PowerMockRunner::class)
class StartUpRepositoryTest {

    private lateinit var startUpRepository: StartUpRepository
    private lateinit var startupApiHelper: StartupApiHelper
    private lateinit var instrumentationContext: Context
    private val packageName = "za.co.woolworths.financial.services.android.models"

    @Before
    fun init() {
        startupApiHelper = mock()
        startUpRepository = StartUpRepository(startupApiHelper)
        instrumentationContext = mock(Context::class.java, RETURNS_DEEP_STUBS)
        RetrofitConfig.mApiInterface = mock()
        mock(Build::class.java)
    }

    // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
    @Ignore
    @Test
    fun check_if_methods_gets_called() = runBlockingTest {
        WoolworthsApplication.testSetInstance(mock())
        `when`(WoolworthsApplication.getInstance().getPackageName()).thenReturn(packageName)
        val packageInfo: PackageInfo = mock()
        packageInfo.versionName = packageName
        val packageManager: PackageManager = mock()
        `when`(WoolworthsApplication.getInstance().packageManager).thenReturn(packageManager)
        `when`(packageManager.getPackageInfo(packageName, 0)).thenReturn(packageInfo)
        WoolworthsApplication.testSetContext(instrumentationContext)
        setFinalStatic(Build::class.java.getField("MANUFACTURER"), "Woolworths")
        setFinalStatic(Build::class.java.getField("MODEL"), "Zensar")
        val telephonyManager: TelephonyManager = mock(TelephonyManager::class.java, RETURNS_DEEP_STUBS)
        `when`(instrumentationContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager)
        `when`(telephonyManager.getNetworkOperatorName()).thenReturn("Airtel")
        startUpRepository.queryServiceGetConfig()
        startUpRepository.clearSharedPreference(instrumentationContext)
        startUpRepository.setSessionDao(SessionDao.KEY.SPLASH_VIDEO, "1")
        Assert.assertTrue(startUpRepository.getSessionDao(SessionDao.KEY.SPLASH_VIDEO))

        verify(startupApiHelper, times(1)).getConfig()
        //verify(sessionDaoSave(SessionDao.KEY.SPLASH_VIDEO, "1"))
        //verify(clearSharedPreferences(instrumentationContext))
    }
}