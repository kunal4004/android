package za.co.woolworths.financial.services.android.ui.activities

import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when` as _when

class StartupActivityTest {
    private lateinit var startupActivity: StartupActivity

    @Before fun setup(){
        try {
            //usage of startupActivity while not initialized
            //should throw an error.
            Assert.assertNull(this.startupActivity)
        }catch (e: UninitializedPropertyAccessException){
            startupActivity = Mockito.mock(StartupActivity::class.java, Mockito.withSettings().verboseLogging())
        }

        //usage of startupActivity should work now
        Assert.assertNotNull(this.startupActivity)
    }


    @Test
    fun testFirebaseRemoteConfig(){

        //real methods to execute
        _when(this.startupActivity.notifyIfNeeded()).thenCallRealMethod()
        _when(this.startupActivity.getmFirebaseRemoteConfig()).thenCallRealMethod()

        Assert.assertNotNull(startupActivity)
        this.startupActivity.notifyIfNeeded()

        //startupActivity
        //startupActivity.notifyIfNeeded()
    }

    @Test
    fun testAppIsExpired(){

        //startupActivity
    }
}