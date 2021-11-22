package za.co.woolworths.financial.services.android.checkout.view

import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.checkout_new_user_recipient_details.*
import kotlinx.android.synthetic.main.vehicle_details_layout.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import za.co.woolworths.financial.services.android.utils.TestCoroutineRule

/**
 * Created by Kunal Uttarwar on 19/11/21.
 */

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class CheckoutWhoIsCollectingFragmentTest : Fragment() {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var checkoutWhoIsCollectingFragment: CheckoutWhoIsCollectingFragment
    private var listOfVehicleInputFields = listOf<View>()
    private var listOfTaxiInputFields = listOf<View>()

    @Before
    fun init() {
        checkoutWhoIsCollectingFragment = CheckoutWhoIsCollectingFragment()

        listOfVehicleInputFields = listOf(
            recipientNameEditText,
            cellphoneNumberEditText,
            vehicleColourEditText,
            vehicleModelEditText
        )
        listOfTaxiInputFields = listOf(recipientNameEditText, cellphoneNumberEditText)
    }

    @Test
    fun testVehicleTaxiListSize() {
        checkoutWhoIsCollectingFragment.initView()
        Assert.assertEquals(
            listOfVehicleInputFields.size,
            checkoutWhoIsCollectingFragment.testGetMyVehicleList().size
        )
        Assert.assertEquals(
            listOfTaxiInputFields.size,
            checkoutWhoIsCollectingFragment.testGetTaxiList().size
        )
    }
}