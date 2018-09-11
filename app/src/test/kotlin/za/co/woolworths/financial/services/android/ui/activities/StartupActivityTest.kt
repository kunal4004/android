package za.co.woolworths.financial.services.android.ui.activities

import android.content.Context
import com.awfs.coordination.R
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import junit.framework.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito.*
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.FirebaseManager
import java.util.*
import org.powermock.api.mockito.PowerMockito.`when` as _when

@RunWith(PowerMockRunner::class)
@PrepareForTest(FirebaseManager::class, WoolworthsApplication::class, FirebaseApp::class, FirebaseRemoteConfig::class)
class StartupActivityTest {

    companion object {

        lateinit var woolworthsApplicationMock: WoolworthsApplication
        lateinit var firebaseAppMock: FirebaseApp
        lateinit var firebaseRemoteConfigMock: FirebaseRemoteConfig

        @BeforeClass @JvmStatic fun beforeClass(){
            woolworthsApplicationMock = mock(WoolworthsApplication::class.java, Mockito.withSettings().verboseLogging())
            firebaseAppMock = mock(FirebaseApp::class.java, Mockito.withSettings().verboseLogging())
            firebaseRemoteConfigMock = mock(FirebaseRemoteConfig::class.java, Mockito.withSettings().verboseLogging())

            mockStatic(WoolworthsApplication::class.java, FirebaseApp::class.java, FirebaseRemoteConfig::class.java)
            _when(WoolworthsApplication.getInstance()).thenReturn(woolworthsApplicationMock)
            _when(FirebaseApp.initializeApp(Mockito.any(Context::class.java))).thenReturn(firebaseAppMock)
            _when(FirebaseApp.getInstance()).thenReturn(firebaseAppMock)
            _when(FirebaseRemoteConfig.getInstance()).thenReturn(firebaseRemoteConfigMock)

            Assert.assertEquals(woolworthsApplicationMock, WoolworthsApplication.getInstance())
            Assert.assertEquals(firebaseAppMock, FirebaseApp.initializeApp(woolworthsApplicationMock))
            Assert.assertEquals(firebaseAppMock, FirebaseApp.getInstance())
        }
    }

    @Before fun setup(){
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testRandomVideos(){
        var startupActivity = mock(StartupActivity::class.java, Mockito.withSettings().verboseLogging())

        val environment = com.awfs.coordination.BuildConfig.FLAVOR
        val mockPackageName = "com.awfs.coordination" + (if (environment == "production") "" else ".$environment")

        //expectations
        _when(startupActivity.packageName).thenReturn(mockPackageName)
        _when(startupActivity.testGetRandomVideos()).thenCallRealMethod()

        //execution
        var random = startupActivity.testGetRandomVideos()

        //tests
        val listOfVideo = ArrayList<String>()
        listOfVideo.add(R.raw.food_broccoli.toString())
        listOfVideo.add(R.raw.food_chocolate.toString())

        var videoResourceName = ""
        for (s: String in listOfVideo){
            if (random.contains(s)){
                videoResourceName = s
                break
            }
        }

        Assert.assertEquals("android.resource://$mockPackageName/$videoResourceName", random)
    }


    /**
     * Returns Mockito.any() as nullable type to avoid java.lang.IllegalStateException when
     * null is returned.
     */
    fun <T> any(): T = Mockito.any<T>()

    @Test
    fun testFirebaseRemoteConfig(){
        var startupActivity = mock(StartupActivity::class.java, Mockito.withSettings().verboseLogging())
        val firebaseManager = mock(FirebaseManager::class.java)

        //real methods to execute
        _when(startupActivity.notifyIfNeeded()).thenCallRealMethod()
        _when(firebaseManager.getRemoteConfig()).thenCallRealMethod()
        _when(firebaseManager.setupRemoteConfig(any())).thenCallRealMethod()

        _when(startupActivity.getFirebaseManager()).thenReturn(firebaseManager)



        //by default, remote config should be null
        Assert.assertNull(firebaseManager.getRemoteConfig())

        //_when(this.firebaseManager.getRemoteConfig()).thenCallRealMethod()
        //


        //_when(FirebaseManager.getInstance()).thenReturn(firebaseManager)

        //Assert.assertNotNull(startupActivity)
        //this.startupActivity.notifyIfNeeded()

        //startupActivity
        //expect firebaseManager.setupRemoteConfig() to be
        //executed in notifyIfNeeded()
        startupActivity.notifyIfNeeded()
    }

    @Test
    fun testAppIsExpired(){

        //startupActivity
    }
}