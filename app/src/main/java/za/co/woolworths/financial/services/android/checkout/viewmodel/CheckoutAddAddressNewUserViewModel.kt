package za.co.woolworths.financial.services.android.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.utils.CheckoutResourceGetProvince
import za.co.woolworths.financial.services.android.checkout.utils.CheckoutResourceGetSuburb

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserViewModel(private val checkoutAddAddressNewUserInteractor: CheckoutAddAddressNewUserInteractor): ViewModel() {

    fun initGetSuburbs(provinceId: String) = liveData(Dispatchers.IO) {
        emit(CheckoutResourceGetSuburb.loading(data = null))
        try {
            emit(CheckoutResourceGetSuburb.success(data = checkoutAddAddressNewUserInteractor.getSuburbs(provinceId).body()))
        } catch (exception: Exception) {
            emit(CheckoutResourceGetSuburb.error(data = null, msg = exception.toString()))
        }
    }

    fun initGetProvince() = liveData(Dispatchers.IO) {
        emit(CheckoutResourceGetProvince.loading(data = null))
        try {
            emit(CheckoutResourceGetProvince.success(data = checkoutAddAddressNewUserInteractor.getProvince().body()))
        } catch (exception: Exception) {
            emit(CheckoutResourceGetProvince.error(data = null, msg = exception.toString()))
        }
    }
}