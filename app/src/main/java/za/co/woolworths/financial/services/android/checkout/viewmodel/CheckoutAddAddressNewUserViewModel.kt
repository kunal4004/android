package za.co.woolworths.financial.services.android.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.utils.CheckoutResource

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserViewModel(private val checkoutAddAddressNewUserInteractor: CheckoutAddAddressNewUserInteractor): ViewModel() {

    fun initGetSuburbs(provinceId: String) = liveData(Dispatchers.IO) {
        emit(CheckoutResource.loading(data = null))
        try {
            emit(CheckoutResource.success(data = checkoutAddAddressNewUserInteractor.getSuburbs(provinceId).body()))
        } catch (exception: Exception) {
            emit(CheckoutResource.error(data = null, msg = exception.toString()))
        }
    }
}