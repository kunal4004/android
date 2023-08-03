package za.co.woolworths.financial.services.android.checkout.view

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.SelectedPlacesAddress
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.app_config.native_checkout.ConfigNativeCheckout
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.utils.BundleMock
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

    private lateinit var checkoutAddAddressNewUserFragment: CheckoutAddAddressNewUserFragment
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @Before
    fun init() {
        checkoutAddAddressNewUserFragment =
            mock(CheckoutAddAddressNewUserFragment::class.java, CALLS_REAL_METHODS)
        firebaseAnalytics = mock()
    }

    @Test
    fun check_if_onlyProvinceEnabled() = runBlockingTest {
        // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
//        val province = Province()
//        province.apply {
//            id = "1"
//            name = "Western Cape"
//        }
//        checkoutAddAddressNewUserFragment.selectedAddress = SelectedPlacesAddress()
//        checkoutAddAddressNewUserFragment.selectedAddress.provinceName = "Free State"
//        checkoutAddAddressNewUserFragment.selectedAddress.savedAddress.suburb = "BaysWater"
//        val mockProvinceList: MutableList<Province> = ArrayList()
//        mockProvinceList.add(province)
//        checkoutAddAddressNewUserFragment.checkIfSelectedProvinceExist(mockProvinceList)
//
//        verify(checkoutAddAddressNewUserFragment, times(1)).disableSuburbSelection()
//        verify(checkoutAddAddressNewUserFragment, times(1)).enableProvinceSelection()
//        verify(checkoutAddAddressNewUserFragment, times(1)).enablePostalCode()
    }

    @Test
    fun check_if_onlySuburbEnabled() = runBlockingTest {
        // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
//        val province = Province()
//        province.apply {
//            id = "1"
//            name = "Western Cape"
//        }
//        checkoutAddAddressNewUserFragment.selectedAddress = SelectedPlacesAddress()
//        checkoutAddAddressNewUserFragment.selectedAddress.provinceName = "Western Cape"
//        checkoutAddAddressNewUserFragment.selectedAddress.savedAddress.suburb = ""
//        val mockProvinceList: MutableList<Province> = ArrayList()
//        mockProvinceList.add(province)
//        checkoutAddAddressNewUserFragment.checkIfSelectedProvinceExist(mockProvinceList)
//
//        verify(checkoutAddAddressNewUserFragment, times(1)).disableProvinceSelection()
//        verify(checkoutAddAddressNewUserFragment, times(1)).resetSuburbSelection()
//        verify(checkoutAddAddressNewUserFragment, times(1)).enableSuburbSelection()
//        verify(checkoutAddAddressNewUserFragment, times(1)).enablePostalCode()
    }

    @Test
    fun check_onProvienceSelected_gets_called() {
        // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
//        checkoutAddAddressNewUserFragment.selectedAddress = SelectedPlacesAddress()
//        val province = Province()
//        province.apply {
//            id = "1"
//            name = "Western Cape"
//        }
//        checkoutAddAddressNewUserFragment.onProvinceSelected(province)
//
//        verify(checkoutAddAddressNewUserFragment, times(1)).enableEditText()
    }

    @Test
    fun check_methodCalls_onSuburbSelected() {
        // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
//        checkoutAddAddressNewUserFragment.deliveryType = DeliveryType.DELIVERY
//        checkoutAddAddressNewUserFragment.selectedAddress = SelectedPlacesAddress()
//        val mockSuburb = Suburb()
//        mockSuburb.apply {
//            name = "Cape Town"
//            id = "123"
//            postalCode = "789"
//        }
//        checkoutAddAddressNewUserFragment.onSuburbSelected(mockSuburb)
//
//        Assert.assertEquals(
//            checkoutAddAddressNewUserFragment.selectedAddress.savedAddress.suburb,
//            mockSuburb.name
//        )
//        Assert.assertEquals(
//            checkoutAddAddressNewUserFragment.selectedAddress.savedAddress.suburbId,
//            mockSuburb.id
//        )
//        Assert.assertEquals(
//            checkoutAddAddressNewUserFragment.selectedAddress.savedAddress.postalCode,
//            mockSuburb.postalCode
//        )
//        verify(checkoutAddAddressNewUserFragment, times(1)).enableEditText()
//        verify(checkoutAddAddressNewUserFragment, times(1)).disablePostalCode()
    }

    // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
    @Ignore
    @Test
    fun showErrorDialog_if_address1_isEmpty() {
        checkoutAddAddressNewUserFragment.selectedAddress = SelectedPlacesAddress()
        doNothing().`when`(checkoutAddAddressNewUserFragment).showErrorDialog()
        checkoutAddAddressNewUserFragment.onSaveAddressClicked()
        verify(checkoutAddAddressNewUserFragment, times(1)).showErrorDialog()
    }

    @Test
    fun show_suburb_not_deliverable_dialog() {
        val mockAddAddressResponse = AddAddressResponse()
        val response = Response()
        response.code =
            SuburbNotDeliverableBottomsheetDialogFragment.ERROR_CODE_SUBURB_NOT_DELIVERABLE

        mockAddAddressResponse.response = response
        checkoutAddAddressNewUserFragment.addAddressErrorResponse(mockAddAddressResponse, 1)
        verify(
            checkoutAddAddressNewUserFragment,
            times(1)
        ).showSuburbNotDeliverableBottomSheetDialog(response.code)
    }

    @Test
    fun show_nickName_exist_error() {
        val mockAddAddressResponse = AddAddressResponse()
        val response = Response()
        response.code = "500"

        mockAddAddressResponse.response = response
        val validationErrorList = ArrayList<ValidationError>()
        val validationError = ValidationError()
        validationError.setField("nickname")
        validationErrorList.add(validationError)
        mockAddAddressResponse.validationErrors = listOf(validationError)
        doNothing().`when`(checkoutAddAddressNewUserFragment).showNickNameExist()
        checkoutAddAddressNewUserFragment.addAddressErrorResponse(mockAddAddressResponse, 1)
        verify(checkoutAddAddressNewUserFragment, times(1)).isNickNameAlreadyExist(
            mockAddAddressResponse
        )
        verify(checkoutAddAddressNewUserFragment, times(1)).showNickNameExist()
    }

    // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
    @Ignore
    @Test
    fun verify_editAddress_bundle() {
        val bundle = BundleMock.mock()
        val savedAddress = SavedAddressResponse()
        val addressList = ArrayList<Address>()
        addressList.add(getAddress())
        addressList.add(getAddress())
        savedAddress.addresses = addressList
        bundle.putString(
            CheckoutAddressConfirmationListAdapter.EDIT_SAVED_ADDRESS_RESPONSE_KEY,
            Utils.toJson(savedAddress)
        )
        bundle.putInt(CheckoutAddressConfirmationListAdapter.EDIT_ADDRESS_POSITION_KEY, 1)
        checkoutAddAddressNewUserFragment.testSetBundleArguments(bundle)
        checkoutAddAddressNewUserFragment.selectedAddress = SelectedPlacesAddress()
        val mockNativeCheckout: ConfigNativeCheckout =
            mock(ConfigNativeCheckout::class.java, CALLS_REAL_METHODS)
        val province = Province()
        province.apply {
            name = "Western Cape"
            id = "1"
        }
        val provinceList = listOf(province)
        mockNativeCheckout.regions = provinceList
        AppConfigSingleton.nativeCheckout = mockNativeCheckout

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

    // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
    @Ignore
    @Test
    fun check_if_newAddress_button_click() {
        val bundle = BundleMock.mock()
        val savedAddress = SavedAddressResponse()
        val addressList = ArrayList<Address>()
        addressList.add(getAddress())
        addressList.add(getAddress())
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
            savedAddress.addresses?.get(0)?.id,
            checkoutAddAddressNewUserFragment.testGetSavedAddress()?.addresses?.get(0)?.id
        )
    }

    @Ignore
    fun checkOnlySavedAddressCall() {
        val bundle = BundleMock.mock()
        val savedAddress = SavedAddressResponse()

        val addressList = ArrayList<Address>()
        addressList.add(getAddress())
        addressList.add(getAddress())
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

    private fun getAddress(): Address {
        val address = Address()
        address.apply {
            id = "1"
            addressType = "Home"
            region = "1"
        }
        return address
    }
}