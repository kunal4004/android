package za.co.woolworths.financial.services.android.ui.fragments.product.utils

import za.co.woolworths.financial.services.android.models.dto.Refinement
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.SubRefinement

interface OnRefinementOptionSelected {
    fun onRefinementOptionSelected(refinementNavigation: RefinementNavigation)
    fun onRefinementSelected(refinement: Refinement)
    fun onSubRefinementSelected(subRefinement: SubRefinement)
    fun onBackPressedWithRefinement(navigationState: String)
    fun onBackPressedWithOutRefinement()
    fun onSeeResultClicked(navigationState: String)
}