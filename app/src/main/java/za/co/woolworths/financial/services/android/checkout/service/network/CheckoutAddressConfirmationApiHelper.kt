package za.co.woolworths.financial.services.android.checkout.service.network

import retrofit2.Response
import za.co.woolworths.financial.services.android.models.dto.SetDeliveryLocationSuburbResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig

/**
 * Created by Kunal Uttarwar on 12/08/21.
 */
class CheckoutAddressConfirmationApiHelper: RetrofitConfig() {
    fun setSuburb(suburbId: String): Response<SetDeliveryLocationSuburbResponse> = OneAppService.setSuburb(suburbId).execute()
}