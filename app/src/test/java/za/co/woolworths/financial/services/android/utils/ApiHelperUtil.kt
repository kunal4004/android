package za.co.woolworths.financial.services.android.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import org.mockito.Mockito
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig

object ApiHelperUtil {

    private lateinit var context: Context
    private val packageName = "za.co.woolworths.financial.services.android.models"

    fun setup() {
        context = Mockito.mock(Context::class.java, Mockito.RETURNS_DEEP_STUBS)
        RetrofitConfig.mApiInterface = mock()
        Mockito.mock(Build::class.java)
        WoolworthsApplication.testSetInstance(mock())
        Mockito.`when`(WoolworthsApplication.getInstance().getPackageName()).thenReturn(packageName)
        val packageInfo: PackageInfo = mock()
        packageInfo.versionName = packageName
        val packageManager: PackageManager = mock()
        Mockito.`when`(WoolworthsApplication.getInstance().packageManager).thenReturn(packageManager)
        Mockito.`when`(packageManager.getPackageInfo(packageName, 0)).thenReturn(packageInfo)
        WoolworthsApplication.testSetContext(context)
        setFinalStatic(Build::class.java.getField("MANUFACTURER"), "Woolworths")
        setFinalStatic(Build::class.java.getField("MODEL"), "Zensar")
        val telephonyManager: TelephonyManager = Mockito.mock(TelephonyManager::class.java, Mockito.RETURNS_DEEP_STUBS)
        Mockito.`when`(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager)
        Mockito.`when`(telephonyManager.getNetworkOperatorName()).thenReturn("Airtel")
    }
}