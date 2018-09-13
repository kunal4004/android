
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import za.co.woolworths.financial.services.android.contracts.OnCompletionListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.FirebaseManager
import org.powermock.api.mockito.PowerMockito.`when` as _when


@RunWith(PowerMockRunner::class)
@PrepareForTest(WoolworthsApplication::class, FirebaseApp::class, FirebaseRemoteConfig::class, FirebaseManager::class)
class FirebaseManagerTest {

    @Before
    fun setup(){
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Returns Mockito.any() as nullable type to avoid java.lang.IllegalStateException when
     * null is returned.
     */
    fun <T> any(): T = Mockito.any<T>()

    @Test
    fun testFirebaseRemoteConfig(){
        //mocks statics
        PowerMockito.mockStatic(WoolworthsApplication::class.java, FirebaseApp::class.java, FirebaseRemoteConfig::class.java)

        //mocks
        val woolworthsApplicationMock = PowerMockito.mock(WoolworthsApplication::class.java, Mockito.withSettings().verboseLogging())
        val firebaseAppMock = PowerMockito.mock(FirebaseApp::class.java, Mockito.withSettings().verboseLogging())
        val firebaseRemoteConfigMock = PowerMockito.mock(FirebaseRemoteConfig::class.java, Mockito.withSettings().verboseLogging())
        val firebaseManagerMock = PowerMockito.mock(FirebaseManager::class.java)
        val taskMock = PowerMockito.mock(Task::class.java) as Task<Void>

        _when(WoolworthsApplication.getInstance()).thenReturn(woolworthsApplicationMock)
        _when(FirebaseApp.initializeApp(Mockito.any())).thenReturn(firebaseAppMock)
        _when(FirebaseApp.getInstance()).thenReturn(firebaseAppMock)
        _when(FirebaseRemoteConfig.getInstance()).thenReturn(firebaseRemoteConfigMock)
        _when(firebaseRemoteConfigMock.fetch()).thenReturn(taskMock)

        _when(firebaseManagerMock.getRemoteConfig()).thenCallRealMethod()
        _when(firebaseManagerMock.setupRemoteConfig(any())).thenCallRealMethod()

        //assertions
        Assert.assertEquals(woolworthsApplicationMock, WoolworthsApplication.getInstance())
        Assert.assertEquals(firebaseAppMock, FirebaseApp.initializeApp(woolworthsApplicationMock))
        Assert.assertEquals(firebaseAppMock, FirebaseApp.getInstance())

        Assert.assertEquals(taskMock, firebaseRemoteConfigMock.fetch())

        //execution
        FirebaseManager.getInstance().setupRemoteConfig(OnCompletionListener {

        })

        //verifications
        Mockito.verify(taskMock, Mockito.times(1)).addOnCompleteListener(any())
    }
}