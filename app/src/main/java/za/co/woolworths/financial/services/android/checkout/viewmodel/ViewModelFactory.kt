package za.co.woolworths.financial.services.android.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddressConfirmationInteractor

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class ViewModelFactory(
    private val interactor: Any
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutAddAddressNewUserViewModel::class.java)) {
            return CheckoutAddAddressNewUserViewModel(
                interactor as CheckoutAddAddressNewUserInteractor
            ) as T
        }
        if (modelClass.isAssignableFrom(CheckoutAddressConfirmationViewModel::class.java)) {
            return CheckoutAddressConfirmationViewModel(
                interactor as CheckoutAddressConfirmationInteractor
            ) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}