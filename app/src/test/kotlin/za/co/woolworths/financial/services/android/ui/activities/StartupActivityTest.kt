package za.co.woolworths.financial.services.android.ui.activities

import android.content.Context
import com.awfs.coordination.R
import com.google.android.gms.tasks.Task
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
        //mocks
        var startupActivityMock = mock(StartupActivity::class.java, Mockito.withSettings().verboseLogging())
        val firebaseManagerMock = mock(FirebaseManager::class.java)
        val taskMock = mock(Task::class.java) as Task<Void>

        //real methods to execute and expectations
        _when(startupActivityMock.notifyIfNeeded()).thenCallRealMethod()
        _when(firebaseManagerMock.getRemoteConfig()).thenCallRealMethod()
        _when(firebaseManagerMock.setupRemoteConfig(any())).thenCallRealMethod()

        _when(startupActivityMock.getFirebaseManager()).thenReturn(firebaseManagerMock)
        _when(firebaseRemoteConfigMock.fetch()).thenReturn(taskMock)

        //by default, remote config should be null
        Assert.assertNull(firebaseManagerMock.getRemoteConfig())
        Assert.assertEquals(firebaseManagerMock, startupActivityMock.getFirebaseManager())
        Assert.assertEquals(taskMock, firebaseRemoteConfigMock.fetch())

        //execution
        startupActivityMock.notifyIfNeeded()

        //verifications
        Mockito.verify(taskMock, Mockito.times(1)).addOnCompleteListener(any())
    }

    @Test
    fun testAppIsExpired(){

        //startupActivity
    }
}