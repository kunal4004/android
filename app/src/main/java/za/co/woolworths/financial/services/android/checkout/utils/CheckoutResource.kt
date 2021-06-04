package za.co.woolworths.financial.services.android.checkout.utils

import retrofit2.Response
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse
import za.co.woolworths.financial.services.android.service.network.ResponseStatus

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
data class CheckoutResource(val responseStatus: ResponseStatus, val data: SuburbsResponse?, val message: String?) {
    companion object {

        fun success(data: SuburbsResponse?): CheckoutResource {
            return CheckoutResource(ResponseStatus.SUCCESS, data, null)
        }

        fun error(msg: String, data: SuburbsResponse?): CheckoutResource {
            return CheckoutResource(ResponseStatus.ERROR, data, msg)
        }

        fun loading(data: SuburbsResponse?): CheckoutResource {
            return CheckoutResource(ResponseStatus.LOADING, data, null)
        }
    }
}
