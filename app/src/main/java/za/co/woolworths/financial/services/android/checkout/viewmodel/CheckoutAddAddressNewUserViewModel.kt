package za.co.woolworths.financial.services.android.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressRequestBody
import za.co.woolworths.financial.services.android.checkout.utils.CheckoutResourceAddAddress
import za.co.woolworths.financial.services.android.checkout.utils.CheckoutResourceGetProvince
import za.co.woolworths.financial.services.android.checkout.utils.CheckoutResourceGetSuburb
import za.co.woolworths.financial.services.android.checkout.utils.CheckoutResourceSavedAddress

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserViewModel(private val checkoutAddAddressNewUserInteractor: CheckoutAddAddressNewUserInteractor) :
    ViewModel() {

    fun initGetSuburbs(provinceId: String) = liveData(Dispatchers.IO) {
        emit(CheckoutResourceGetSuburb.loading(data = null))
        try {
            emit(
                CheckoutResourceGetSuburb.success(
                    data = checkoutAddAddressNewUserInteractor.getSuburbs(
                        provinceId
                    ).body()
                )
            )
        } catch (exception: Exception) {
            emit(CheckoutResourceGetSuburb.error(data = null, msg = exception.toString()))
        }
    }

    fun initGetProvince() = liveData(Dispatchers.IO) {
        emit(CheckoutResourceGetProvince.loading(data = null))
        try {
            emit(
                CheckoutResourceGetProvince.success(
                    data = checkoutAddAddressNewUserInteractor.getProvince().body()
                )
            )
        } catch (exception: Exception) {
            emit(CheckoutResourceGetProvince.error(data = null, msg = exception.toString()))
        }
    }

    fun getSavedAddresses() = liveData(Dispatchers.IO) {
        emit(CheckoutResourceSavedAddress.loading(data = null))
        try {
            emit(
                CheckoutResourceSavedAddress.success(
                    data = checkoutAddAddressNewUserInteractor.getSavedAddresses().body()
                )
            )
        } catch (exception: Exception) {
            emit(CheckoutResourceSavedAddress.error(data = null, msg = exception.toString()))
        }
    }

    fun addAddress(addAddressRequestBody: AddAddressRequestBody) = liveData(Dispatchers.IO) {
        emit(CheckoutResourceAddAddress.loading(data = null))
        try {
            emit(
                CheckoutResourceAddAddress.success(
                    data = checkoutAddAddressNewUserInteractor.addAddress(
                        addAddressRequestBody
                    ).body()
                )
            )
        } catch (exception: Exception) {
            emit(CheckoutResourceAddAddress.error(data = null, msg = exception.toString()))
        }
    }
}