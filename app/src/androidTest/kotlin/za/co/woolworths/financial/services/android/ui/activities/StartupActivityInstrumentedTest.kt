package za.co.woolworths.financial.services.android.ui.activities

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.awfs.coordination.R
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import za.co.woolworths.financial.services.android.contracts.OnApiCompletionListener
import za.co.woolworths.financial.services.android.models.dao.MobileConfigServerDao
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.util.HttpAsyncTask
import za.co.woolworths.financial.services.android.util.NetworkManager
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
public class StartupActivityInstrumentedTest {

    @Rule @JvmField
    var activityRule = ActivityTestRule(StartupActivity::class.java, false, false)

    val countDownLatch = CountDownLatch(1)

    @Before
    fun setup(){
        //tried this in attempt to keep the same
        //activity open for all tests in this class
        if (activityRule.activity == null)
            activityRule.launchActivity(null)
    }

    @Test
    fun testSplashScreen(){
        //1. When not connected to network, error screen is presented
        //2. When connected and first time, a splash video is displayed
        //3. When connected and not first time, loading dialog is displayed

        if (!NetworkManager.getInstance().isConnectedToNetwork(activityRule.activity)){
            onView(withId(R.id.errorLayout)).check(matches(isDisplayed()))
        }
        else if (activityRule.activity.testIsFirstTime()){
            onView(withId(R.id.videoViewLayout)).check(matches(isDisplayed()))
        }else{
            onView(withId(R.id.splashNoVideoView)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testAppContext(){

        val appPackageName = "com.awfs.coordination.qa"

        val appContext = InstrumentationRegistry.getTargetContext()
        Assert.assertEquals(appPackageName, appContext.packageName)
    }

    @Test
    fun testMobileConfigServer(){
        var success = false

        MobileConfigServerDao.queryServiceGetConfig(activityRule.activity, object :OnApiCompletionListener<ConfigResponse>{
            override fun success(responseObject: ConfigResponse) {
                Assert.assertTrue(responseObject is ConfigResponse)
                success = true
                countDownLatch.countDown()
            }

            override fun failure(errorMessage: String?, httpErrorCode: HttpAsyncTask.HttpErrorCode?) {
                success = false
                countDownLatch.countDown()
            }
        })

        this.countDownLatch.await()
        Assert.assertTrue(success)
    }
}