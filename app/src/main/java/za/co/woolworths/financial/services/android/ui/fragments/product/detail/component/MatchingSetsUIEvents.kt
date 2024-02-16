package za.co.woolworths.financial.services.android.ui.fragments.product.detail.component

/**
 * Created by Kunal Uttarwar on 17/02/24.
 */
sealed class MatchingSetsUIEvents {
    data class seeMoreClick(val isSeeMore: Boolean) : MatchingSetsUIEvents()
}