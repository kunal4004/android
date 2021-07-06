package za.co.woolworths.financial.services.android.checkout.utils

import za.co.woolworths.financial.services.android.checkout.service.network.DeleteAddressResponse
import za.co.woolworths.financial.services.android.service.network.ResponseStatus

/**
 * Created by Kunal Uttarwar on 05/07/21.
 */
data class CheckoutResourceDeleteAddress(
    val responseStatus: ResponseStatus,
    val data: DeleteAddressResponse?,
    val message: String?
) {
    companion object {

        fun success(data: DeleteAddressResponse?): CheckoutResourceDeleteAddress {
            return CheckoutResourceDeleteAddress(ResponseStatus.SUCCESS, data, null)
        }

        fun error(msg: String, data: DeleteAddressResponse?): CheckoutResourceDeleteAddress {
            return CheckoutResourceDeleteAddress(ResponseStatus.ERROR, data, msg)
        }

        fun loading(data: DeleteAddressResponse?): CheckoutResourceDeleteAddress {
            return CheckoutResourceDeleteAddress(ResponseStatus.LOADING, data, null)
        }
    }
}
