package za.co.woolworths.financial.services.android.checkout.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.repository.CheckoutLiquorRepository
import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressRequestBody
import za.co.woolworths.financial.services.android.checkout.service.network.ConfirmSelectionRequestBody
import za.co.woolworths.financial.services.android.checkout.service.network.ShippingDetailsBody
import za.co.woolworths.financial.services.android.checkout.utils.NativeCheckoutResource
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.StorePickupInfoBody
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
@HiltViewModel
class CheckoutAddAddressNewUserViewModel @Inject constructor
    (
    private val checkoutAddAddressNewUserInteractor: CheckoutAddAddressNewUserInteractor,
    private val checkoutLiquorRepository: CheckoutLiquorRepository,
) :
    ViewModel() {

    private val _shoppingCartData = MutableLiveData<Event<Resource<ShoppingCartResponse>>>()
    val shoppingCartData: LiveData<Event<Resource<ShoppingCartResponse>>> = _shoppingCartData

    init {
        getShoppingCartData()
    }

    fun getShoppingCartData() {
        viewModelScope.launch {
            val shoppingCartResponse = checkoutLiquorRepository.getShoppingCartData()
            _shoppingCartData.value = Event(shoppingCartResponse)
        }
    }

    fun validateSelectedSuburb(suburbId: String, isStore: Boolean) = liveData(Dispatchers.IO) {
        emit(NativeCheckoutResource.loading(data = null))
        try {
            emit(
                NativeCheckoutResource.success(
                    data = checkoutAddAddressNewUserInteractor.validateSelectedSuburb(
                        suburbId,
                        isStore
                    ).body()
                )
            )
        } catch (exception: Exception) {
            emit(NativeCheckoutResource.error(data = null, msg = exception.toString()))
        }
    }

    fun addAddress(addAddressRequestBody: AddAddressRequestBody): LiveData<Any> {
        return checkoutAddAddressNewUserInteractor.addAddress(addAddressRequestBody)
    }

    fun editAddress(
        addAddressRequestBody: AddAddressRequestBody,
        addressId: String,
    ): LiveData<Any> {
        return checkoutAddAddressNewUserInteractor.editAddress(addAddressRequestBody, addressId)
    }

    fun deleteAddress(addressId: String): LiveData<Any> {
        return checkoutAddAddressNewUserInteractor.deleteAddress(addressId)
    }

    fun changeAddress(nickName: String): LiveData<Any> {
        return checkoutAddAddressNewUserInteractor.changeAddress(nickName)
    }

    fun getShippingDetails(body: ShippingDetailsBody): LiveData<Any> {
        return checkoutAddAddressNewUserInteractor.getShippingDetails(body)
    }

    fun setConfirmSelection(confirmSelectionRequestBody: ConfirmSelectionRequestBody): LiveData<Any> {
        return checkoutAddAddressNewUserInteractor.setConfirmSelection(confirmSelectionRequestBody)
    }

    fun getStorePickupInfo(body: StorePickupInfoBody): LiveData<Any> {
        return checkoutAddAddressNewUserInteractor.getStorePickupInfo(body)
    }

    fun getConfirmLocationDetails(body: ConfirmLocationRequest): LiveData<Any> {
        return checkoutAddAddressNewUserInteractor.getConfirmLocationDetails(body)
    }
    fun verifyUserIsInStore(latitude: Double, longitude: Double): LiveData<Any> {
        return checkoutAddAddressNewUserInteractor.verifyUserIsInStore(latitude, longitude)
    }
}