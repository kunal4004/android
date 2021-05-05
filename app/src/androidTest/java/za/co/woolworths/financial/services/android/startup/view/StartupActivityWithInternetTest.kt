package za.co.woolworths.financial.services.android.startup.view


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.awfs.coordination.R
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class StartupActivityWithInternetTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(StartupActivity::class.java)

    @Test
    fun checkElementsWhenHavingInternet() {
        val imageView = onView(
                allOf(withId(R.id.splashLogo),
                        withParent(allOf(withId(R.id.splashNoVideoView),
                                withParent(withId(R.id.mainFrameLayout)))),
                        isDisplayed()))
        imageView.check(matches(isDisplayed()))

        val progressBar = onView(
                allOf(withId(R.id.progressBar),
                        withParent(allOf(withId(R.id.splashNoVideoView),
                                withParent(withId(R.id.mainFrameLayout)))),
                        isDisplayed()))
        progressBar.check(matches(isDisplayed()))
    }
}
