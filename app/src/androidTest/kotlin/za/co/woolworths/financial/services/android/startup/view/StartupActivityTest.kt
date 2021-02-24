package za.co.woolworths.financial.services.android.startup.view


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4

import com.awfs.coordination.R
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import za.co.woolworths.financial.services.android.util.NetworkManager

@LargeTest
@RunWith(AndroidJUnit4::class)
class StartupActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(StartupActivity::class.java)

    @Test
    fun checkElementsWhenHavingInternet() {
        assertTrue(NetworkManager.getInstance().isConnectedToNetwork(mActivityTestRule.getActivity()))
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

    @Test
    fun checkElementsWithoutInternet() {
        assertFalse(NetworkManager.getInstance().isConnectedToNetwork(mActivityTestRule.getActivity()))
        val imageView = onView(
                allOf(withId(R.id.splashLogo),
                        withParent(allOf(withId(R.id.splashNoVideoView),
                                withParent(withId(R.id.mainFrameLayout)))),
                        isDisplayed()))
        imageView.check(matches(isDisplayed()))

        val textView = onView(
                allOf(withId(R.id.somethingWentWrongTV), withText("OOPS. SOMETHING WENT WRONG"),
                        withParent(allOf(withId(R.id.errorLayout),
                                withParent(withId(R.id.splashNoVideoView)))),
                        isDisplayed()))
        textView.check(matches(withText("OOPS. SOMETHING WENT WRONG")))

        val textView2 = onView(
                allOf(withId(R.id.somethingWentWrongTV), withText("OOPS. SOMETHING WENT WRONG"),
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

        val textView4 = onView(
                allOf(withId(R.id.noInternetConnectionTV), withText("Please ensure that you are connected \nto the internet."),
                        withParent(allOf(withId(R.id.errorLayout),
                                withParent(withId(R.id.splashNoVideoView)))),
                        isDisplayed()))
        textView4.check(matches(withText("Please ensure that you are connected  to the internet.")))

        val button = onView(
                allOf(withId(R.id.retry), withText("RETRY"),
                        withParent(allOf(withId(R.id.errorLayout),
                                withParent(withId(R.id.splashNoVideoView)))),
                        isDisplayed()))
        button.check(matches(isDisplayed()))
    }
}
