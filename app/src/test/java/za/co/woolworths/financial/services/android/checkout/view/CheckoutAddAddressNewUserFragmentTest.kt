package za.co.woolworths.financial.services.android.checkout.view

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.Response
import za.co.woolworths.financial.services.android.checkout.service.network.ValidationError
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.SelectedPlacesAddress
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.util.DeliveryType
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
    fun check_if_onlyProvinceEnabled() = runBlockingTest {

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

    @Test
    fun check_if_onlySuburbEnabled() = runBlockingTest {
        val province = Province()
        province.apply {
            id = "1"
            name = "Western Cape"
        }
        checkoutAddAddressNewUserFragment.selectedAddress = SelectedPlacesAddress()
        checkoutAddAddressNewUserFragment.selectedAddress.provinceName = "Western Cape"
        checkoutAddAddressNewUserFragment.selectedAddress.savedAddress.suburb = ""
        val mockProvinceList: MutableList<Province> = ArrayList()
        mockProvinceList.add(province)
        checkoutAddAddressNewUserFragment.checkIfSelectedProvinceExist(mockProvinceList)

        verify(checkoutAddAddressNewUserFragment, times(1)).disableProvinceSelection()
        verify(checkoutAddAddressNewUserFragment, times(1)).resetSuburbSelection()
        verify(checkoutAddAddressNewUserFragment, times(1)).enableSuburbSelection()
        verify(checkoutAddAddressNewUserFragment, times(1)).enablePostalCode()
    }

    @Test
    fun check_onProvienceSelected_gets_called() {
        checkoutAddAddressNewUserFragment.selectedAddress = SelectedPlacesAddress()
        val province = Province()
        province.apply {
            id = "1"
            name = "Western Cape"
        }
        checkoutAddAddressNewUserFragment.onProvinceSelected(province)

        verify(checkoutAddAddressNewUserFragment, times(1)).enableEditText()
    }

    @Test
    fun check_methodCalls_onSuburbSelected() {
        checkoutAddAddressNewUserFragment.deliveryType = DeliveryType.DELIVERY
        checkoutAddAddressNewUserFragment.selectedAddress = SelectedPlacesAddress()
        val mockSuburb = Suburb()
        mockSuburb.apply {
            name = "Cape Town"
            id = "123"
            postalCode = "789"
        }
        checkoutAddAddressNewUserFragment.onSuburbSelected(mockSuburb)

        Assert.assertEquals(
            checkoutAddAddressNewUserFragment.selectedAddress.savedAddress.suburb,
            mockSuburb.name
        )
        Assert.assertEquals(
            checkoutAddAddressNewUserFragment.selectedAddress.savedAddress.suburbId,
            mockSuburb.id
        )
        Assert.assertEquals(
            checkoutAddAddressNewUserFragment.selectedAddress.savedAddress.postalCode,
            mockSuburb.postalCode
        )
        verify(checkoutAddAddressNewUserFragment, times(1)).enableEditText()
        verify(checkoutAddAddressNewUserFragment, times(1)).disablePostalCode()
    }

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
}