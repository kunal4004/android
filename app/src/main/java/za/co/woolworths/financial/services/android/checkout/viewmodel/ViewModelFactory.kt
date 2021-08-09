package za.co.woolworths.financial.services.android.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class ViewModelFactory(
    private val checkoutAddAddressNewUserInteractor: CheckoutAddAddressNewUserInteractor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutAddAddressNewUserViewModel::class.java)) {
            return CheckoutAddAddressNewUserViewModel(
                checkoutAddAddressNewUserInteractor
            ) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}