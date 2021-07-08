package za.co.woolworths.financial.services.android.checkout.utils

import za.co.woolworths.financial.services.android.service.network.ResponseStatus

/**
 * Created by Kunal Uttarwar on 13/06/21.
 */
data class NativeCheckoutResource(
    val responseStatus: ResponseStatus,
    val data: Any?,
    val message: String?
) {
    companion object {

        fun success(data: Any?): NativeCheckoutResource {
            return NativeCheckoutResource(ResponseStatus.SUCCESS, data, null)
        }

        fun error(msg: String, data: Any?): NativeCheckoutResource {
            return NativeCheckoutResource(ResponseStatus.ERROR, data, msg)
        }

        fun loading(data: Any?): NativeCheckoutResource {
            return NativeCheckoutResource(ResponseStatus.LOADING, data, null)
        }
    }
}
