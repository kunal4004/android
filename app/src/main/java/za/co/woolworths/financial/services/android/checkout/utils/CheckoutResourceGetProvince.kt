package za.co.woolworths.financial.services.android.checkout.utils

import za.co.woolworths.financial.services.android.models.dto.ProvincesResponse
import za.co.woolworths.financial.services.android.service.network.ResponseStatus

/**
 * Created by Kunal Uttarwar on 05/06/21.
 */
data class CheckoutResourceGetProvince(
    val responseStatus: ResponseStatus,
    val data: ProvincesResponse?,
    val message: String?
) {
    companion object {

        fun success(data: ProvincesResponse?): CheckoutResourceGetProvince {
            return CheckoutResourceGetProvince(ResponseStatus.SUCCESS, data, null)
        }

        fun error(msg: String, data: ProvincesResponse?): CheckoutResourceGetProvince {
            return CheckoutResourceGetProvince(ResponseStatus.ERROR, data, msg)
        }

        fun loading(data: ProvincesResponse?): CheckoutResourceGetProvince {
            return CheckoutResourceGetProvince(ResponseStatus.LOADING, data, null)
        }
    }
}
