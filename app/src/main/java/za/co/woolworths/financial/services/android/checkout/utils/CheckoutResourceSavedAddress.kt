package za.co.woolworths.financial.services.android.checkout.utils

import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.models.dto.ProvincesResponse
import za.co.woolworths.financial.services.android.service.network.ResponseStatus

/**
 * Created by Kunal Uttarwar on 10/06/21.
 */
data class CheckoutResourceSavedAddress(
    val responseStatus: ResponseStatus,
    val data: SavedAddressResponse?,
    val message: String?
) {
    companion object {

        fun success(data: SavedAddressResponse?): CheckoutResourceSavedAddress {
            return CheckoutResourceSavedAddress(ResponseStatus.SUCCESS, data, null)
        }

        fun error(msg: String, data: SavedAddressResponse?): CheckoutResourceSavedAddress {
            return CheckoutResourceSavedAddress(ResponseStatus.ERROR, data, msg)
        }

        fun loading(data: SavedAddressResponse?): CheckoutResourceSavedAddress {
            return CheckoutResourceSavedAddress(ResponseStatus.LOADING, data, null)
        }
    }
}

