package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import androidx.test.filters.LargeTest
import androidx.fragment.app.testing.launchFragmentInContainer

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner

import com.awfs.coordination.R
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4ClassRunner::class)
class ReviewDetailsFragmentTest {

    @Test
    fun reviewDetailsOneTest() {
        launchFragmentInContainer<ReviewDetailsFragment>(null,
                R.style.Theme_AppCompat_Transparent_NoActionBar)

        onView(ViewMatchers.withId(R.id.rating_bar)).check(matches(isDisplayed()))

        val tvSkinProfileLabel = onView(ViewMatchers.withId(R.id.tv_skin_label)).check(matches(isDisplayed()))

        tvSkinProfileLabel.check(matches(withText(R.string.perfect_skin_label)))

        onView(ViewMatchers.withId(R.id.iv_like)).check(matches(isDisplayed()))

        onView(ViewMatchers.withId(R.id.tv_skin_label)).check(matches(isDisplayed()))
    }
}
