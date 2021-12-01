package za.co.woolworths.financial.services.android.checkout.view

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import za.co.woolworths.financial.services.android.checkout.utils.NativeCheckoutResource
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.SelectedPlacesAddress
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.utils.TestCoroutineRule
import za.co.woolworths.financial.services.android.utils.mock

/**
 * Created by Kunal Uttarwar on 02/12/21.
 */

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class CheckoutAddAddressNewUserFragmentTest : Fragment() {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private lateinit var checkoutAddAddressNewUserFragment: CheckoutAddAddressNewUserFragment

    @Before
    fun init() {
        checkoutAddAddressNewUserFragment =
            mock(CheckoutAddAddressNewUserFragment::class.java, CALLS_REAL_METHODS)
        checkoutAddAddressNewUserViewModel = mock()
        checkoutAddAddressNewUserFragment.testSetViewModelInstance(
            checkoutAddAddressNewUserViewModel
        )
    }

    @Test
    fun check_if_onlyProvinceEnabled()= runBlockingTest {

        val province = Province()
        province.apply {
            id = "1"
            name = "Western Cape"
        }
        checkoutAddAddressNewUserFragment.selectedAddress = SelectedPlacesAddress()
        checkoutAddAddressNewUserFragment.selectedAddress.provinceName = "Free State"
        checkoutAddAddressNewUserFragment.selectedAddress.savedAddress.suburb = "BaysWater"
        val mockProvinceList: MutableList<Province> = ArrayList()
        mockProvinceList.add(province)
        checkoutAddAddressNewUserFragment.checkIfSelectedProvinceExist(mockProvinceList)
        verify(checkoutAddAddressNewUserFragment, times(1)).disableSuburbSelection()
        verify(checkoutAddAddressNewUserFragment, times(1)).enableProvinceSelection()
        verify(checkoutAddAddressNewUserFragment, times(1)).enablePostalCode()
    }
}