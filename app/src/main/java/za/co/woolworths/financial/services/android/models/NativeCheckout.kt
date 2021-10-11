package za.co.woolworths.financial.services.android.models

import za.co.woolworths.financial.services.android.models.dto.CurrentShoppingBag
import za.co.woolworths.financial.services.android.models.dto.NewShoppingBag
import za.co.woolworths.financial.services.android.models.dto.Province

/**
 * Created by Kunal Uttarwar on 07/06/21.
 */
data class NativeCheckout(val isNativeCheckoutEnabled: Boolean, val addressTypes: List<String>, val regions: List<Province>, val checkoutPaymentURL: String?, val checkoutPostPaymentURL: String?, val currentShoppingBag: CurrentShoppingBag, val newShoppingBag: NewShoppingBag)
