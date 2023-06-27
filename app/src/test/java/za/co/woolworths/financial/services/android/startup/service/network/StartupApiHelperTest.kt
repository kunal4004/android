package za.co.woolworths.financial.services.android.startup.service.network

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
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
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.utils.mock
import za.co.woolworths.financial.services.android.utils.setFinalStatic

/**
 * Created by Kunal Uttarwar on 3/3/21.
 */

@RunWith(PowerMockRunner::class)
class StartupApiHelperTest {

    private lateinit var startupApiHelper: StartupApiHelper
    private lateinit var instrumentationContext: Context
    private val packageName = "za.co.woolworths.financial.services.android.models"

    @Before
    fun init() {
        instrumentationContext = mock(Context::class.java, RETURNS_DEEP_STUBS)
        RetrofitConfig.mApiInterface = mock()
        mock(Build::class.java)
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

        startupApiHelper = mock(StartupApiHelper::class.java, CALLS_REAL_METHODS)
    }

    // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
    @Ignore
    @Test
    fun check_if_config_method_get_called() = runBlockingTest {
        startupApiHelper.getConfig()
        verify(RetrofitConfig.mApiInterface).getConfig("ANDROID_Vza.co.woolworths.financial.services.android",
                "363d1f4695e51234849733f0c46be51eff6cd892",
                "Woolworths")
    }

    // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
    @Ignore
    @Test
    fun check_if_internet_is_Off() {
        val connectivityManager: ConnectivityManager = mock(ConnectivityManager::class.java, RETURNS_DEEP_STUBS)
        val mockNetInfo: NetworkInfo = mock(NetworkInfo::class.java)
        val mockNetInfoArray = arrayOf(mockNetInfo)
        `when`(instrumentationContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
        `when`(connectivityManager.getAllNetworkInfo()).thenReturn(mockNetInfoArray)
        Assert.assertFalse(startupApiHelper.isConnectedToInternet(instrumentationContext))
    }
}