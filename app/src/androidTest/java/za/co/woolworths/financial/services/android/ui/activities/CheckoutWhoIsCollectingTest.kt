package za.co.woolworths.financial.services.android.ui.activities


import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.awfs.coordination.R
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment

@LargeTest
@RunWith(AndroidJUnit4::class)
class CheckoutWhoIsCollectingTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(CheckoutActivity::class.java, true, false)

    @Before
    fun initialiseClassData() {
        var intent = Intent()

        val jsonFileString = "{\n" +
                "  \"addresses\": [\n" +
                "    {\n" +
                "      \"id\": \"371945319\",\n" +
                "      \"nickname\": \"test-2\",\n" +
                "      \"recipientName\": \"test1\",\n" +
                "      \"address1\": \"Tesselaarsdal\",\n" +
                "      \"address2\": \"\",\n" +
                "      \"postalCode\": \"7441\",\n" +
                "      \"primaryContactNo\": \"8764786598\",\n" +
                "      \"suburb\": \"Arabella Estate\",\n" +
                "      \"region\": \"2000030\",\n" +
                "      \"suburbId\": \"4400005\",\n" +
                "      \"displayName\": \"test-2\",\n" +
                "      \"storeAddress\": false,\n" +
                "      \"addressType\": \"Office\",\n" +
                "      \"placesId\": \"ChIJc2GlRKYBzh0R7JFQcXTc_1E\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"primaryContactNo\": \"\",\n" +
                "  \"defaultAddressNickname\": \"\",\n" +
                "  \"isStorePickup\": false,\n" +
                "  \"response\": {\n" +
                "    \"code\": \"-1\",\n" +
                "    \"desc\": \"Success\"\n" +
                "  },\n" +
                "  \"httpCode\": 200\n" +
                "}"

        var mockSavedAddressResponse: SavedAddressResponse =
            Gson().fromJson(jsonFileString, object : TypeToken<SavedAddressResponse>() {}.type)
        intent.putExtra(
            CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY,
            mockSavedAddressResponse
        )
        intent.putExtra(CheckoutAddressManagementBaseFragment.IS_DELIVERY, false)
        mActivityTestRule.launchActivity(intent)
    }

    @Test
    fun whoIsCollectingTest() {
        val button = onView(
            allOf(
                withId(R.id.confirmDetails), withText("CONFIRM DETAILS"),
                withParent(withParent(withId(R.id.collectionDetailsNestedScrollView))),
                isDisplayed()
            )
        )
        button.check(matches(isDisplayed()))

        val textView3 = onView(
            allOf(
                withId(R.id.recipientDetailsTitle),
                withText("Who is Collecting?"),
                withContentDescription("lableRecipientDetailsTitle"),
                withParent(withParent(withId(R.id.whoIsCollectingDetailsLayout))),
                isDisplayed()
            )
        )
        textView3.check(matches(withText("Who is Collecting?")))

        val textView = onView(
            allOf(
                withId(R.id.myVehicleText), withText("My Vehicle"),
                withParent(
                    allOf(
                        withId(R.id.vehicleTypeLayout),
                        withParent(withId(R.id.scrollView))
                    )
                ),
                isDisplayed()
            )
        )
        textView.check(matches(withText("My Vehicle")))

        val textView2 = onView(
            allOf(
                withId(R.id.taxiText), withText("E-Hailing"),
                withParent(
                    allOf(
                        withId(R.id.vehicleTypeLayout),
                        withParent(withId(R.id.scrollView))
                    )
                ),
                isDisplayed()
            )
        )
        textView2.check(matches(withText("E-Hailing")))
    }
}
