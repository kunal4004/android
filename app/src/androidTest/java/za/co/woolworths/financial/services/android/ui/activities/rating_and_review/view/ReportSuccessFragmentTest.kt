package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.awfs.coordination.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class ReportSuccessFragmentTest {

    @Test
    fun test_ReportSuccesScreenData() {
        launchFragmentInContainer<ReportSuccessFragment>()
        onView(ViewMatchers.withId(R.id.img_success))
                .check(matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.txt_view_report_submitted))
                .check(matches(ViewMatchers.withText(R.string.report_submit)))
        onView(ViewMatchers.withId(R.id.txt_view_moderated))
                .check(matches(ViewMatchers.withText(R.string.report_moderated)))
    }
}