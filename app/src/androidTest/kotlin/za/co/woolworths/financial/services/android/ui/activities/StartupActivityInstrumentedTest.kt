package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith



@SmallTest
@RunWith(AndroidJUnit4::class)
public class StartupActivityInstrumentedTest {

    @Rule @JvmField
    val activityRule = ActivityTestRule(StartupActivity::class.java)

    private val appPackageName: String = "com.awfs.coordination.qa"

    @Test
    fun testAppContext(){
        val appContext = InstrumentationRegistry.getTargetContext()
        Assert.assertEquals(this.appPackageName, appContext.packageName)
    }

    @Test
    fun testIntent(){
        val intent = Intent()

        activityRule.launchActivity(intent)

        Assert.assertNotNull(activityRule.activity)

    }
}