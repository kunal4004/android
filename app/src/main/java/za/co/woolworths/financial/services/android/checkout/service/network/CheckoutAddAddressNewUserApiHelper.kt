package za.co.woolworths.financial.services.android.checkout.service.network

import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.util.DeliveryType

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserApiHelper: RetrofitConfig() {

    fun getSuburbs(provinceId: String) = OneAppService.getSuburbs(provinceId)
}