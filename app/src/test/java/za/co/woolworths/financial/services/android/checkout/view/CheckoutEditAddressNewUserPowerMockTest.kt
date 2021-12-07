package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.SelectedPlacesAddress
import za.co.woolworths.financial.services.android.models.NativeCheckout
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.utils.BundleMock
import za.co.woolworths.financial.services.android.utils.mock


/**
 * Created by Kunal Uttarwar on 07/12/21.
 */

@RunWith(PowerMockRunner::class)
@PrepareForTest(Bundle::class)
class CheckoutEditAddressNewUserPowerMockTest {

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
    fun verify_editAddress_bundle() {
        val bundle = BundleMock.mock()
        val savedAddress = SavedAddressResponse()
        val address = Address()
        address.apply {
            id = "1"
            addressType = "Home"
            region = "1"
        }
        val addressList = ArrayList<Address>()
        addressList.add(address)
        addressList.add(address)
        savedAddress.addresses = addressList
        bundle.putString(
            CheckoutAddressConfirmationListAdapter.EDIT_SAVED_ADDRESS_RESPONSE_KEY,
            Utils.toJson(savedAddress)
        )
        bundle.putInt(CheckoutAddressConfirmationListAdapter.EDIT_ADDRESS_POSITION_KEY, 1)
        checkoutAddAddressNewUserFragment.testSetBundleArguments(bundle)
        checkoutAddAddressNewUserFragment.selectedAddress = SelectedPlacesAddress()
        val mockNativeCheckout: NativeCheckout =
            mock(NativeCheckout::class.java, CALLS_REAL_METHODS)
        val province = Province()
        province.apply {
            name = "Western Cape"
            id = "1"
        }
        val provinceList = listOf(province)
        mockNativeCheckout.regions = provinceList
        WoolworthsApplication.setNativeCheckout(mockNativeCheckout)

        checkoutAddAddressNewUserFragment.handleBundleResponse()

        Assert.assertEquals(
            checkoutAddAddressNewUserFragment.testGetSelectedAddressId(),
            addressList[1].id
        )
        Assert.assertEquals(
            checkoutAddAddressNewUserFragment.selectedAddress.provinceName,
            province.name
        )
        Assert.assertEquals(
            checkoutAddAddressNewUserFragment.testGetSelectedDeliveryAddressType(),
            addressList[1].addressType
        )
        Assert.assertEquals(
            checkoutAddAddressNewUserFragment.selectedAddress.savedAddress.id,
            addressList[1].id
        )

    }
}