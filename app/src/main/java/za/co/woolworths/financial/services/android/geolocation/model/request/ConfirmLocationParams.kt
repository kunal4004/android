package za.co.woolworths.financial.services.android.geolocation.model.request

import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem

/**
 * Created by Kunal Uttarwar on 21/06/23.
 */
data class ConfirmLocationParams(val commerceItemList: ArrayList<UnSellableCommerceItem>?, val confirmLocationRequest: ConfirmLocationRequest?)