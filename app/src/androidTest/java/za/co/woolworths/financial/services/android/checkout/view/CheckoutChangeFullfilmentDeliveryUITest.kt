package za.co.woolworths.financial.services.android.checkout.view


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
import org.hamcrest.core.IsInstanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse


@LargeTest
@RunWith(AndroidJUnit4::class)
class CheckoutChangeFullfilmentDeliveryUITest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(CheckoutActivity::class.java, true, false)

    @Before
    fun initialiseClassData() {
        val intent = Intent()
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

        val mockSavedAddressResponse: SavedAddressResponse =
            Gson().fromJson(jsonFileString, object : TypeToken<SavedAddressResponse>() {}.type)
        intent.putExtra(
            CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY,
            mockSavedAddressResponse
        )
        intent.putExtra(CheckoutAddressManagementBaseFragment.IS_DELIVERY, true)
        mActivityTestRule.launchActivity(intent)
    }

    @Test
    fun checkoutChangeFullfilmentDelivery() {
        val textView = onView(
            allOf(withId(R.id.deliveryTab), withText("Delivery"),
                withParent(withParent(withId(R.id.deliveryOrCollectionLayout))),
                isDisplayed()))
        textView.check(matches(withText("Delivery")))

        val imageView = onView(
            allOf(withId(R.id.editAddressImageView), withContentDescription("Edit Address"),
                withParent(allOf(withId(R.id.addressSelectionLayout),
                    withParent(withId(R.id.saveAddressRecyclerView)))),
                isDisplayed()))
        imageView.check(matches(isDisplayed()))

        val textView2 = onView(
            allOf(withId(R.id.addNewAddressTextView), withText("Add A New Address"),
                withParent(allOf(withId(R.id.addressConfirmationDelivery),
                    withParent(IsInstanceOf.instanceOf(android.view.ViewGroup::class.java)))),
                isDisplayed()))
        textView2.check(matches(isDisplayed()))

        val button = onView(
            allOf(withId(R.id.btnAddressConfirmation), withText("CONFIRM"),
                withParent(allOf(withId(R.id.btnConfirmLayout),
                    withParent(IsInstanceOf.instanceOf(android.view.ViewGroup::class.java)))),
                isDisplayed()))
        button.check(matches(isDisplayed()))

        val imageButton = onView(
            allOf(withContentDescription("Navigate up"),
                withParent(allOf(withId(R.id.toolbar),
                    withParent(withId(R.id.appbar)))),
                isDisplayed()))
        imageButton.check(matches(isDisplayed()))

        val imageButton2 = onView(
            allOf(withContentDescription("Navigate up"),
                withParent(allOf(withId(R.id.toolbar),
                    withParent(withId(R.id.appbar)))),
                isDisplayed()))
        imageButton2.check(matches(isDisplayed()))
    }
}
