package za.co.woolworths.financial.services.android.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddressConfirmationInteractor
import za.co.woolworths.financial.services.android.checkout.utils.NativeCheckoutResource

/**
 * Created by Kunal Uttarwar on 12/08/21.
 */
class CheckoutAddressConfirmationViewModel(private val checkoutAddressConfirmationInteractor: CheckoutAddressConfirmationInteractor) :
    ViewModel() {
    fun setSuburb(suburbId: String) = liveData(Dispatchers.IO) {
        emit(NativeCheckoutResource.loading(data = null))
        try {
            emit(
                NativeCheckoutResource.success(
                    data = checkoutAddressConfirmationInteractor.setSuburb(
                        suburbId
                    ).body()
                )
            )
        } catch (exception: Exception) {
            emit(NativeCheckoutResource.error(data = null, msg = exception.toString()))
        }
    }
}