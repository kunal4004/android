package za.co.woolworths.financial.services.android.ui.activities


import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.awfs.coordination.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.startup.view.StartupActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class CheckoutAddAddressNewUserFragmentUITest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(CheckoutActivity::class.java, true, false)

    @Before
    fun initialiseClassData() {
        var intent = Intent()
        mActivityTestRule.launchActivity(intent)
    }

        @Test
    fun checkoutAddAddressNewUserFragmentUITest() {

        val textView = onView(
            allOf(
                withId(R.id.whereWeDeliveringTitle), withText("Where Are We Delivering?"),
                withParent(withParent(withId(R.id.newUserNestedScrollView))),
                isDisplayed()
            )
        )
        textView.check(matches(withText("Where Are We Delivering?")))

        val editText = onView(
            allOf(
                withId(R.id.progressbarGetSuburb),
                withParent(
                    allOf(
                        withId(R.id.selectSuburbLayout),
                        withParent(withId(R.id.recipientAddressLayout))
                    )
                ),
                isDisplayed()
            )
        )
        editText.check(doesNotExist())

        val editText4 = onView(
            allOf(
                withId(R.id.dropdownGetSuburbImg),
                withParent(
                    allOf(
                        withId(R.id.selectSuburbLayout),
                        withParent(withId(R.id.recipientAddressLayout))
                    )
                ),
                isDisplayed()
            )
        )
        editText4.check(doesNotExist())

        val editText5 = onView(
            allOf(
                withId(R.id.dropdownGetSuburbImg),
                withParent(
                    allOf(
                        withId(R.id.selectSuburbLayout),
                        withParent(withId(R.id.recipientAddressLayout))
                    )
                ),
                isDisplayed()
            )
        )
        editText5.check(doesNotExist())

        val editText2 = onView(
            allOf(
                withId(R.id.progressbarGetProvinces),
                withParent(
                    allOf(
                        withId(R.id.selectProvinceLayout),
                        withParent(withId(R.id.recipientAddressLayout))
                    )
                ),
                isDisplayed()
            )
        )
        editText2.check(doesNotExist())

        val editText3 = onView(
            allOf(
                withId(R.id.dropdownGetProvincesImg),
                withParent(
                    allOf(
                        withId(R.id.selectProvinceLayout),
                        withParent(withId(R.id.recipientAddressLayout))
                    )
                ),
                isDisplayed()
            )
        )
        editText3.check(doesNotExist())
    }
}
