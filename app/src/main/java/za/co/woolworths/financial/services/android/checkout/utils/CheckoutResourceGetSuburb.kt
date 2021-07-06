package za.co.woolworths.financial.services.android.checkout.utils

import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse
import za.co.woolworths.financial.services.android.service.network.ResponseStatus

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
data class CheckoutResourceGetSuburb(val responseStatus: ResponseStatus, val data: SuburbsResponse?, val message: String?) {
    companion object {

        fun success(data: SuburbsResponse?): CheckoutResourceGetSuburb {
            return CheckoutResourceGetSuburb(ResponseStatus.SUCCESS, data, null)
        }

        fun error(msg: String, data: SuburbsResponse?): CheckoutResourceGetSuburb {
            return CheckoutResourceGetSuburb(ResponseStatus.ERROR, data, msg)
        }

        fun loading(data: SuburbsResponse?): CheckoutResourceGetSuburb {
            return CheckoutResourceGetSuburb(ResponseStatus.LOADING, data, null)
        }
    }
}
