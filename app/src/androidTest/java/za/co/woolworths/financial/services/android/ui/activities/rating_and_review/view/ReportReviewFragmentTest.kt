package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.widget.ScrollView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.awfs.coordination.R
import org.hamcrest.core.AllOf.allOf
import org.hamcrest.core.IsInstanceOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class ReportReviewFragmentTest {

    @Test
    fun test_ReportReviewScreenData() {
        val sceanario  = launchFragmentInContainer<ReportReviewFragment>()
        onView(ViewMatchers.withId(R.id.txt_report_label))
                .check(matches(ViewMatchers.withText(R.string.report_reviews)))
        onView(ViewMatchers.withId(R.id.review_sub_label))
                .check(matches(ViewMatchers.withText(R.string.report_sub_label)))
        onView(ViewMatchers.withId(R.id.btn_submit_report))
                .check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun test_SubmitButton() {
       onView(allOf(ViewMatchers.withId(R.id.btn_submit_report),
               ViewMatchers.withText("SUBMIT REPORT"),
               ViewMatchers.withParent(ViewMatchers.withParent(
                       IsInstanceOf.instanceOf(ScrollView::class.java))),
              ViewMatchers.isDisplayed()))
    }
}
