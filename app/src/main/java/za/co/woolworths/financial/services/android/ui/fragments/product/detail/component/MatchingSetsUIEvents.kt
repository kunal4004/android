package za.co.woolworths.financial.services.android.ui.fragments.product.detail.component

import za.co.woolworths.financial.services.android.models.dto.ProductRequest

/**
 * Created by Kunal Uttarwar on 17/02/24.
 */
sealed class MatchingSetsUIEvents {
    data class seeMoreClick(val isSeeMore: Boolean) : MatchingSetsUIEvents()
    data class quickShopClick(val productRequest: ProductRequest) : MatchingSetsUIEvents()
}