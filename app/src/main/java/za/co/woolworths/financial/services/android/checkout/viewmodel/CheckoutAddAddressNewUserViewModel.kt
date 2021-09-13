package za.co.woolworths.financial.services.android.checkout.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressRequestBody
import za.co.woolworths.financial.services.android.checkout.utils.NativeCheckoutResource
import za.co.woolworths.financial.services.android.models.network.ConfirmDeliveryAddressBody

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserViewModel(private val checkoutAddAddressNewUserInteractor: CheckoutAddAddressNewUserInteractor) :
    ViewModel() {

    fun initGetSuburbs(provinceId: String) = liveData(Dispatchers.IO) {
        emit(NativeCheckoutResource.loading(data = null))
        try {
            emit(
                NativeCheckoutResource.success(
                    data = checkoutAddAddressNewUserInteractor.getSuburbs(
                        provinceId
                    ).body()
                )
            )
        } catch (exception: Exception) {
            emit(NativeCheckoutResource.error(data = null, msg = exception.toString()))
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

    fun updateAddress(addAddressRequestBody: AddAddressRequestBody, addressId: String) =
        liveData(Dispatchers.IO) {
            emit(NativeCheckoutResource.loading(data = null))
            try {
                emit(
                    NativeCheckoutResource.success(
                        data = checkoutAddAddressNewUserInteractor.updateAddress(
                            addAddressRequestBody, addressId
                        ).body()
                    )
                )
            } catch (exception: Exception) {
                emit(NativeCheckoutResource.error(data = null, msg = exception.toString()))
            }
        }

    fun deleteAddress(addressId: String): LiveData<Any> {
        return checkoutAddAddressNewUserInteractor.deleteAddress(addressId)
    }

    fun getAvailableDeliverySlots() = liveData(Dispatchers.IO) {
        emit(NativeCheckoutResource.loading(data = null))
        try {
            emit(
                NativeCheckoutResource.success(
                    data = checkoutAddAddressNewUserInteractor.getAvailableDeliverySlots().body()
                )
            )
        } catch (exception: Exception) {
            emit(NativeCheckoutResource.error(data = null, msg = exception.toString()))
        }
    }

    fun getConfirmDeliveryAddressDetails(body: ConfirmDeliveryAddressBody): LiveData<Any> {
        return checkoutAddAddressNewUserInteractor.getConfirmDeliveryAddressDetails(body)
    }

    fun changeAddress(nickName: String) : LiveData<Any> {
       return checkoutAddAddressNewUserInteractor.changeAddress(nickName)
    }
}