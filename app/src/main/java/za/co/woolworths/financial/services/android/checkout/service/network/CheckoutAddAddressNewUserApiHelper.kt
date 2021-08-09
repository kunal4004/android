package za.co.woolworths.financial.services.android.checkout.service.network

import retrofit2.Response
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserApiHelper: RetrofitConfig() {

    fun getSuburbs(provinceId: String): Response<SuburbsResponse> = OneAppService.getSuburbs(provinceId).execute()
    fun validateSelectedSuburb(suburbId: String, isStore: Boolean): Response<ValidateSelectedSuburbResponse> = OneAppService.validateSelectedSuburb(suburbId, isStore).execute()
}