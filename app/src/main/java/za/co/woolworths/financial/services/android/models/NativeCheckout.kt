package za.co.woolworths.financial.services.android.models

import za.co.woolworths.financial.services.android.models.dto.Province

/**
 * Created by Kunal Uttarwar on 07/06/21.
 */
data class NativeCheckout(val isNativeCheckoutEnabled: Boolean, val addressTypes: List<String>, val regions: List<Province>)
