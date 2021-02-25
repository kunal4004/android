package za.co.woolworths.financial.services.android.startup.view

import android.os.SystemClock
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.awfs.coordination.R
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Kunal Uttarwar on 25/2/21.
 */

@LargeTest
@RunWith(AndroidJUnit4::class)
class StartupActivityWithoutInternetTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(StartupActivity::class.java)

    @Before
    fun init() {
        DisableWiFi()
    }

    @Test
    fun checkElementsWithoutInternet() {
        SystemClock.sleep(4000)
        val imageView = onView(
                allOf(withId(R.id.splashLogo),
                        withParent(allOf(withId(R.id.splashNoVideoView),
                                withParent(withId(R.id.mainFrameLayout)))),
                        isDisplayed()))
        imageView.check(matches(isDisplayed()))

        val textView = onView(
                allOf(withId(R.id.somethingWentWrongTV),
                        withParent(allOf(withId(R.id.errorLayout),
                                withParent(withId(R.id.splashNoVideoView)))),
                        isDisplayed()))
        textView.check(matches(withText("OOPS. SOMETHING WENT WRONG")))

        val textView2 = onView(
                allOf(withId(R.id.somethingWentWrongTV),
                        withParent(allOf(withId(R.id.errorLayout),
                                withParent(withId(R.id.splashNoVideoView)))),
                        isDisplayed()))
        textView2.check(matches(isDisplayed()))

        val textView3 = onView(
                allOf(withId(R.id.noInternetConnectionTV), withText("Please ensure that you are connected \nto the internet."),
                        withParent(allOf(withId(R.id.errorLayout),
                                withParent(withId(R.id.splashNoVideoView)))),
                        isDisplayed()))
        textView3.check(matches(isDisplayed()))

        val button = onView(
                allOf(withId(R.id.retry), withText("RETRY"),
                        withParent(allOf(withId(R.id.errorLayout),
                                withParent(withId(R.id.splashNoVideoView)))),
                        isDisplayed()))
        button.check(matches(isDisplayed()))

        EnableWiFi()
    }

    fun EnableWiFi() {
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("svc wifi enable")
    }

    fun DisableWiFi() {
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("svc wifi disable")
    }
}
