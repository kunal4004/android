package za.co.woolworths.financial.services.android.ui.fragments.product.detail.component

import za.co.woolworths.financial.services.android.models.dto.RelatedProducts

/**
 * Created by Kunal Uttarwar on 06/02/24.
 */
data class MatchingSetData(
    val relatedProducts: ArrayList<RelatedProducts> = arrayListOf(),
    val matchingSetDetails: List<MatchingSetDetails>
)
