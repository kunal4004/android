package za.co.woolworths.financial.services.android.startup.service.network

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.verify
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
        instrumentationContext = Mockito.mock(Context::class.java, Mockito.RETURNS_DEEP_STUBS)
        RetrofitConfig.mApiInterface = mock()
        Mockito.mock(Build::class.java)
        WoolworthsApplication.testSetInstance(mock())
        Mockito.`when`(WoolworthsApplication.getInstance().getPackageName()).thenReturn(packageName)
        val packageInfo: PackageInfo = mock()
        packageInfo.versionName = packageName
        val packageManager: PackageManager = mock()
        Mockito.`when`(WoolworthsApplication.getInstance().packageManager).thenReturn(packageManager)
        Mockito.`when`(packageManager.getPackageInfo(packageName, 0)).thenReturn(packageInfo)
        WoolworthsApplication.testSetContext(instrumentationContext)
        setFinalStatic(Build::class.java.getField("MANUFACTURER"), "Woolworths")
        setFinalStatic(Build::class.java.getField("MODEL"), "Zensar")
        val telephonyManager: TelephonyManager = Mockito.mock(TelephonyManager::class.java, Mockito.RETURNS_DEEP_STUBS)
        Mockito.`when`(instrumentationContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager)
        Mockito.`when`(telephonyManager.getNetworkOperatorName()).thenReturn("Airtel")

        startupApiHelper = Mockito.mock(StartupApiHelper::class.java, Mockito.CALLS_REAL_METHODS)
    }

    @Test
    fun check_if_config_method_get_called() = runBlockingTest {
        startupApiHelper.getConfig()
        verify(RetrofitConfig.mApiInterface).getConfig("ANDROID_Vza.co.woolworths.financial.services.android",
                "363d1f4695e51234849733f0c46be51eff6cd892",
                "Woolworths",
                "Zensar",
                "Airtel",
                "Android",
                "0",
                ".",
                "za.co.woolworths.financial.services.android.models")
    }

    @Ignore
    fun check_if_internet_is_on() {
        startupApiHelper.isConnectedToInternet(instrumentationContext)
    }
}