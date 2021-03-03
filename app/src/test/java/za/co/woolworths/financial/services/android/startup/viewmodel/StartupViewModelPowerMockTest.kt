package za.co.woolworths.financial.services.android.startup.viewmodel

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import com.awfs.coordination.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import org.hamcrest.CoreMatchers
import org.hamcrest.core.IsNull
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import org.powermock.modules.junit4.PowerMockRunner
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.startup.service.repository.StartUpRepository
import za.co.woolworths.financial.services.android.utils.mock


/**
 * Created by Kunal Uttarwar on 3/3/21.
 */

@RunWith(PowerMockRunner::class)
class StartupViewModelPowerMockTest {

    private lateinit var startUpRepository: StartUpRepository
    private lateinit var startupApiHelper: StartupApiHelper
    private lateinit var startupViewModel: StartupViewModel
    private lateinit var instrumentationContext: Context
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    val packageInfo: PackageInfo = mock()
    val packageManager: PackageManager = mock()
    private val packageName = "za.co.woolworths.financial.services.android.models"

    @Before
    fun setUp() {
        startUpRepository = mock()
        startupApiHelper = mock()
        mock(Build::class.java)
        mock(SystemClock::class.java)
        RetrofitConfig.mApiInterface = mock()
        firebaseAnalytics = mock()
        instrumentationContext = PowerMockito.mock(Context::class.java)
        startupViewModel = mock(StartupViewModel::class.java, CALLS_REAL_METHODS)

        `when`(instrumentationContext.getPackageName()).thenReturn(packageName)
        packageInfo.versionName = packageName
        `when`(instrumentationContext.packageManager).thenReturn(packageManager)
        doReturn(firebaseAnalytics).`when`(startupViewModel).getFirebaseInstance(instrumentationContext)
    }

    @Test
    fun check_for_environment_variable() {
        `when`(packageManager.getPackageInfo(packageName, 0)).thenReturn(packageInfo)

        startupViewModel.setUpEnvironment(instrumentationContext)
        Assert.assertEquals(BuildConfig.ENV, startupViewModel.environment)
        Assert.assertEquals(packageName, startupViewModel.appVersion)
        Assert.assertThat(startupViewModel.firebaseAnalytics, CoreMatchers.`is`(IsNull.notNullValue()))
    }

    @Test
    fun throws_NameNotFoundException() {

        `when`(packageManager.getPackageInfo(packageName, 0)).thenThrow(PackageManager.NameNotFoundException())

        startupViewModel.setUpEnvironment(instrumentationContext)
        Assert.assertEquals("QA", startupViewModel.environment)
        Assert.assertEquals("6.1.0", startupViewModel.appVersion)
        Assert.assertThat(startupViewModel.firebaseAnalytics, CoreMatchers.`is`(IsNull.notNullValue()))
    }

    @Test
    fun check_for_firebase_events() {
        `when`(packageManager.getPackageInfo(packageName, 0)).thenReturn(packageInfo)
        startupViewModel.setUpEnvironment(instrumentationContext)
        startupViewModel.setUpFirebaseEvents()
        verify(startupViewModel, times(1)).setupFirebaseUserProperty()
    }
}