package za.co.woolworths.financial.services.android.checkout.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddressConfirmationInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressRequestBody
import za.co.woolworths.financial.services.android.checkout.utils.NativeCheckoutResource

/**
 * Created by Kunal Uttarwar on 12/08/21.
 */
class CheckoutAddressConfirmationViewModel(private val checkoutAddressConfirmationInteractor: CheckoutAddressConfirmationInteractor) :
    ViewModel() {
    fun setSuburb(suburbId: String) : LiveData<Any> {
        return checkoutAddressConfirmationInteractor.setSuburb(suburbId)
    }
}