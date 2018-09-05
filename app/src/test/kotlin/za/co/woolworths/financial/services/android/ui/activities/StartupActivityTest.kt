package za.co.woolworths.financial.services.android.ui.activities

import com.awfs.coordination.R
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.*
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
    fun testRandomVideos(){
        val environment = com.awfs.coordination.BuildConfig.FLAVOR
        val mockPackageName = "com.awfs.coordination" + (if (environment == "production") "" else ".$environment")

        //expectations
        _when(this.startupActivity.packageName).thenReturn(mockPackageName)
        _when(this.startupActivity.testGetRandomVideos()).thenCallRealMethod()

        //execution
        var random = this.startupActivity.testGetRandomVideos()

        //test setup
        val listOfVideo = ArrayList<String>()
        listOfVideo.add(R.raw.food_broccoli.toString())
        listOfVideo.add(R.raw.food_chocolate.toString())

        //tests
        var videoResourceName = ""
        for (s: String in listOfVideo){
            if (random.contains(s)){
                videoResourceName = s
                break
            }
        }

        Assert.assertEquals("android.resource://$mockPackageName/$videoResourceName", random)
    }


    /*@Test
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
    }*/
}