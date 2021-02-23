package za.co.woolworths.financial.services.android.ui.activities

import android.content.pm.PackageManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.awfs.coordination.R
import com.microsoft.appcenter.espresso.Factory
import org.junit.*
import org.junit.runner.RunWith
import za.co.woolworths.financial.services.android.startup.view.StartupActivity
import za.co.woolworths.financial.services.android.util.NetworkManager
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
public class StartupActivityInstrumentedTest {

    @Rule @JvmField
    var activityRule = ActivityTestRule(StartupActivity::class.java, false, false)
    var reportHelper = Factory.getReportHelper()

    val countDownLatch = CountDownLatch(1)

    @Before
    fun setup(){
        //tried this in attempt to keep the same
        //activity open for all tests in this class
        if (activityRule.activity == null)
            activityRule.launchActivity(null)
    }

    @After
    fun tearDown(){
        reportHelper.label("Stopping App");
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
    fun testMobileConfigServer() {
        var success = false

        var appVersion = ""
        var environment = ""

        try {
            appVersion = activityRule.activity.getPackageManager().getPackageInfo(activityRule.activity.getPackageName(), 0).versionName
            environment = com.awfs.coordination.BuildConfig.FLAVOR
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val mcsAppVersion = appVersion.substring(0, 3) + if (environment === "production") "" else "-$environment"
    }
        //TODO:: Replace with retrofit 2 implementation
//        MobileConfigServerDao.getConfig(mcsAppVersion, Utils.getUniqueDeviceID(activityRule.activity), object {
//            override fun success(responseObject: ConfigResponse) {
//                Assert.assertTrue(responseObject is ConfigResponse)
//                success = true
//            }
//
//            override fun failure(errorMessage: String?, httpErrorCode: HttpAsyncTask.HttpErrorCode?) {
//                //TODO: handle failure
//            }
//
//            override fun complete() {
//                countDownLatch.countDown()
//            }
//        })
//
//        this.countDownLatch.await()
//        Assert.assertTrue(success)
//    }
}