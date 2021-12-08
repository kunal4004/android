package za.co.woolworths.financial.services.android.checkout.view

import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
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

@RunWith(MockitoJUnitRunner::class)
class CheckoutEditAddressNewUserPowerMockTest {

    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private lateinit var checkoutAddAddressNewUserFragment: CheckoutAddAddressNewUserFragment
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @Before
    fun init() {
        checkoutAddAddressNewUserFragment =
            mock(CheckoutAddAddressNewUserFragment::class.java, CALLS_REAL_METHODS)
        checkoutAddAddressNewUserViewModel = mock()
        checkoutAddAddressNewUserFragment.testSetViewModelInstance(
            checkoutAddAddressNewUserViewModel
        )
        firebaseAnalytics = mock()
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
            addressList[1].id, checkoutAddAddressNewUserFragment.testGetSelectedAddressId()
        )
        Assert.assertEquals(
            province.name, checkoutAddAddressNewUserFragment.selectedAddress.provinceName
        )
        Assert.assertEquals(
            addressList[1].addressType,
            checkoutAddAddressNewUserFragment.testGetSelectedDeliveryAddressType()
        )
        Assert.assertEquals(
            addressList[1].id, checkoutAddAddressNewUserFragment.selectedAddress.savedAddress.id
        )

    }

    @Test
    fun check_if_newAddress_button_click() {
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
            CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY,
            Utils.toJson(savedAddress)
        )
        bundle.putBoolean(CheckoutAddressConfirmationFragment.ADD_NEW_ADDRESS_KEY, true)
        checkoutAddAddressNewUserFragment.testSetBundleArguments(bundle)
        checkoutAddAddressNewUserFragment.handleBundleResponse()

        Assert.assertEquals(true, checkoutAddAddressNewUserFragment.testGetIsAddNewAddress())
        Assert.assertEquals(
            savedAddress?.addresses?.get(0)?.id,
            checkoutAddAddressNewUserFragment.testGetSavedAddress()?.addresses?.get(0)?.id
        )
    }

    @Ignore
    fun checkOnlySavedAddressCall() {
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
            CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY,
            Utils.toJson(savedAddress)
        )
        checkoutAddAddressNewUserFragment.testSetBundleArguments(bundle)
        mock(Utils::class.java)
        doNothing().`when`(Utils.triggerFireBaseEvents(anyString(), anyMap(), any()))
        checkoutAddAddressNewUserFragment.handleBundleResponse()

        Assert.assertEquals(false, checkoutAddAddressNewUserFragment.testGetIsAddNewAddress())
        Assert.assertEquals(
            "", checkoutAddAddressNewUserFragment.testGetSelectedAddressId()
        )
    }
}