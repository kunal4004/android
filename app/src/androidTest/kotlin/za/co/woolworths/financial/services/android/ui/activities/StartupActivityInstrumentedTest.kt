package za.co.woolworths.financial.services.android.ui.activities

import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import za.co.woolworths.financial.services.android.models.dao.ApiResponseHandler
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.util.HttpAsyncTask

@RunWith(AndroidJUnit4::class)
public class StartupActivityInstrumentedTest {

    @Rule @JvmField
    val activityRule = ActivityTestRule(StartupActivity::class.java)

    @Test
    fun testAppContext(){
        val appPackageName = "com.awfs.coordination.qa"

        val appContext = InstrumentationRegistry.getTargetContext()
        Assert.assertEquals(appPackageName, appContext.packageName)
    }

    @Test
    fun testMobileConfigServer(){
        activityRule.activity.testQueryServiceGetConfig(object: ApiResponseHandler {
            override fun success(responseObject: Any) {

                Assert.assertTrue(responseObject is ConfigResponse)
            }

            override fun failure(errorMessage: String?, httpErrorCode: HttpAsyncTask.HttpErrorCode?) {
                Assert.fail()
            }
        });
    }
}