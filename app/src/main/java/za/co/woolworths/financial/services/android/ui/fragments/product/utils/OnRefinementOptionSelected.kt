package za.co.woolworths.financial.services.android.ui.fragments.product.utils

import za.co.woolworths.financial.services.android.models.dto.Refinement
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.SubRefinement

interface OnRefinementOptionSelected {
    fun onRefinementOptionSelected(refinementNavigation: RefinementNavigation)
    fun onRefinementSelected(refinement: Refinement)
    fun onBackPressedWithRefinement(navigationState: String)
    fun onBackPressedWithOutRefinement()
    fun onSeeResults(navigationState: String)
    fun onRefinementClear()
    fun onRefinementReset()
    fun onCategorySelected(refinement: Refinement)
    fun hideCloseButton(){}
    fun hideBackButton(){}
    fun setPageTitle(title: String){}
}