package za.co.woolworths.financial.services.android.checkout.utils

import za.co.woolworths.financial.services.android.checkout.service.network.ChangeAddressResponse
import za.co.woolworths.financial.services.android.service.network.ResponseStatus

/**
 * Created by Kunal Uttarwar on 21/06/21.
 */
data class CheckoutResourceChangeAddress(
    val responseStatus: ResponseStatus,
    val data: ChangeAddressResponse?,
    val message: String?
) {
    companion object {

        fun success(data: ChangeAddressResponse?): CheckoutResourceChangeAddress {
            return CheckoutResourceChangeAddress(ResponseStatus.SUCCESS, data, null)
        }

        fun error(msg: String, data: ChangeAddressResponse?): CheckoutResourceChangeAddress {
            return CheckoutResourceChangeAddress(ResponseStatus.ERROR, data, msg)
        }

        fun loading(data: ChangeAddressResponse?): CheckoutResourceChangeAddress {
            return CheckoutResourceChangeAddress(ResponseStatus.LOADING, data, null)
        }
    }
}
