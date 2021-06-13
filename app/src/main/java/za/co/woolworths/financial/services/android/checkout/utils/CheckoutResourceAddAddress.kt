package za.co.woolworths.financial.services.android.checkout.utils

import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressResponse
import za.co.woolworths.financial.services.android.service.network.ResponseStatus

/**
 * Created by Kunal Uttarwar on 13/06/21.
 */
data class CheckoutResourceAddAddress(
    val responseStatus: ResponseStatus,
    val data: AddAddressResponse?,
    val message: String?
) {
    companion object {

        fun success(data: AddAddressResponse?): CheckoutResourceAddAddress {
            return CheckoutResourceAddAddress(ResponseStatus.SUCCESS, data, null)
        }

        fun error(msg: String, data: AddAddressResponse?): CheckoutResourceAddAddress {
            return CheckoutResourceAddAddress(ResponseStatus.ERROR, data, msg)
        }

        fun loading(data: AddAddressResponse?): CheckoutResourceAddAddress {
            return CheckoutResourceAddAddress(ResponseStatus.LOADING, data, null)
        }
    }
}
