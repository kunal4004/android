package za.co.woolworths.financial.services.android.ui.activities


import android.content.Intent
import android.view.ViewGroup
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
import org.junit.Ignore
import org.junit.Rule
import org.junit.runner.RunWith
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment

@LargeTest
@RunWith(AndroidJUnit4::class)
class CheckoutReturningCollectionTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(CheckoutActivity::class.java, true, false)

    // ToDo Once existing user implementation in collection journey will be done, then only we can continue with this class.

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

    @Ignore
    fun checkoutReturningCollectionTest() {
        val viewGroup = onView(
            allOf(
                withId(R.id.checkoutCollectingFromLayout),
                withParent(withParent(withId(R.id.checkoutReturningCollectionScrollView))),
                isDisplayed()
            )
        )
        viewGroup.check(matches(isDisplayed()))

        val textView = onView(
            allOf(
                withId(R.id.tvNativeCheckoutDeliveringTitle), withText("Collecting From"),
                withParent(
                    allOf(
                        withId(R.id.deliveringTitleShimmerFrameLayout),
                        withParent(withId(R.id.checkoutCollectingFromLayout))
                    )
                ),
                isDisplayed()
            )
        )
        textView.check(matches(withText("Collecting From")))

        val textView2 = onView(
            allOf(
                withId(R.id.tvNativeCheckoutDeliveringValue),
                withParent(
                    allOf(
                        withId(R.id.deliveringTitleValueShimmerFrameLayout),
                        withParent(withId(R.id.checkoutCollectingFromLayout))
                    )
                ),
                isDisplayed()
            )
        )
        textView2.check(matches(isDisplayed()))

        val imageView = onView(
            allOf(
                withId(R.id.imageViewCaretForward),
                withParent(
                    allOf(
                        withId(R.id.forwardImgViewShimmerFrameLayout),
                        withParent(withId(R.id.checkoutCollectingFromLayout))
                    )
                ),
                isDisplayed()
            )
        )
        imageView.check(matches(isDisplayed()))

        val viewGroup2 = onView(
            allOf(
                withId(R.id.checkoutCollectingUserInfoLayout),
                withParent(withParent(withId(R.id.checkoutReturningCollectionScrollView))),
                isDisplayed()
            )
        )
        viewGroup2.check(matches(isDisplayed()))

        val textView3 = onView(
            allOf(
                withId(R.id.tvCollectionUserName),
                withParent(
                    allOf(
                        withId(R.id.tvCollectionUserNameShimmerFrameLayout),
                        withParent(IsInstanceOf.instanceOf(android.view.ViewGroup::class.java))
                    )
                ),
                isDisplayed()
            )
        )
        textView3.check(matches(isDisplayed()))

        val textView4 = onView(
            allOf(
                withId(R.id.tvCollectionUserPhoneNumber),
                withParent(
                    allOf(
                        withId(R.id.tvCollectionUserPhoneNumberShimmerFrameLayout),
                        withParent(IsInstanceOf.instanceOf(android.view.ViewGroup::class.java))
                    )
                ),
                isDisplayed()
            )
        )
        textView4.check(matches(isDisplayed()))

        val textView5 = onView(
            allOf(
                withId(R.id.txtFoodSubstitutionTitle), withText("Food Substitutions"),
                withParent(
                    allOf(
                        withId(R.id.foodSubstitutionTitleShimmerFrameLayout),
                        withParent(withId(R.id.nativeCheckoutFoodSubstitutionLayout))
                    )
                ),
                isDisplayed()
            )
        )
        textView5.check(matches(withText("Food Substitutions")))

        val radioButton = onView(
            allOf(
                withId(R.id.radioBtnSimilarSubst),
                withText("Substitute for similar items if one is available"),
                withParent(
                    allOf(
                        withId(R.id.radioGroupFoodSubstitution),
                        withParent(withId(R.id.radioGroupFoodSubstitutionShimmerFrameLayout))
                    )
                ),
                isDisplayed()
            )
        )
        radioButton.check(matches(isDisplayed()))

        val textView6 = onView(
            allOf(
                withId(R.id.txtSpecialDeliveryInstruction),
                withText("Special Delivery Instructions"),
                withParent(
                    allOf(
                        withId(R.id.instructionTxtShimmerFrameLayout),
                        withParent(withId(R.id.layoutDeliveryInstructions))
                    )
                ),
                isDisplayed()
            )
        )
        textView6.check(matches(withText("Special Delivery Instructions")))

        val switch_ = onView(
            allOf(
                withId(R.id.switchSpecialDeliveryInstruction), withText("OFF"),
                withParent(
                    allOf(
                        withId(R.id.specialInstructionSwitchShimmerFrameLayout),
                        withParent(withId(R.id.layoutDeliveryInstructions))
                    )
                ),
                isDisplayed()
            )
        )
        switch_.check(matches(isDisplayed()))

        val textView7 = onView(
            allOf(
                withId(R.id.txtContinueToPaymentCollection), withText("CONTINUE TO PAYMENT"),
                withParent(
                    allOf(
                        withId(R.id.continuePaymentTxtCollectionShimmerFrameLayout),
                        withParent(IsInstanceOf.instanceOf(android.view.ViewGroup::class.java))
                    )
                ),
                isDisplayed()
            )
        )
        textView7.check(matches(withText("CONTINUE TO PAYMENT")))

        val textView8 = onView(
            allOf(
                withId(R.id.txtContinueToPaymentCollection), withText("CONTINUE TO PAYMENT"),
                withParent(
                    allOf(
                        withId(R.id.continuePaymentTxtCollectionShimmerFrameLayout),
                        withParent(IsInstanceOf.instanceOf(android.view.ViewGroup::class.java))
                    )
                ),
                isDisplayed()
            )
        )
        textView8.check(matches(withText("CONTINUE TO PAYMENT")))
    }
}
