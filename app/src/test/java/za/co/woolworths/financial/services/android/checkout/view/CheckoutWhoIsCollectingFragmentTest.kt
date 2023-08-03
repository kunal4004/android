package za.co.woolworths.financial.services.android.checkout.view

import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
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
            checkoutWhoIsCollectingFragment.binding.whoIsCollectingDetailsLayout.recipientNameEditText,
            checkoutWhoIsCollectingFragment.binding.whoIsCollectingDetailsLayout.cellphoneNumberEditText,
            checkoutWhoIsCollectingFragment.binding.vehiclesDetailsLayout.vehicleColourEditText,
            checkoutWhoIsCollectingFragment.binding.vehiclesDetailsLayout.vehicleModelEditText
        )
        listOfTaxiInputFields = listOf(
            checkoutWhoIsCollectingFragment.binding.whoIsCollectingDetailsLayout.recipientNameEditText,
            checkoutWhoIsCollectingFragment.binding.whoIsCollectingDetailsLayout.cellphoneNumberEditText
        )
    }

    // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
    @Ignore
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