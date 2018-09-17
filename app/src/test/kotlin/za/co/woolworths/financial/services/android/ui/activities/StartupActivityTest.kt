package za.co.woolworths.financial.services.android.ui.activities

import com.awfs.coordination.R
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito.*
import java.util.*
import org.powermock.api.mockito.PowerMockito.`when` as _when

class StartupActivityTest {

    lateinit var startupActivityMock: StartupActivity

    @Before fun setup(){
        MockitoAnnotations.initMocks(this)

        startupActivityMock = mock(StartupActivity::class.java, Mockito.withSettings().verboseLogging())
    }

    @Test
    fun testRandomVideos(){

        val environment = com.awfs.coordination.BuildConfig.FLAVOR
        val mockPackageName = "com.awfs.coordination" + (if (environment == "production") "" else ".$environment")

        //expectations
        _when(startupActivityMock.packageName).thenReturn(mockPackageName)
        _when(startupActivityMock.testGetRandomVideos()).thenCallRealMethod()

        //execution
        var random = startupActivityMock.testGetRandomVideos()

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
}