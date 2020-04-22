package za.co.woolworths.financial.services.android.ui.fragments.product.utils

import za.co.woolworths.financial.services.android.models.dto.Refinement
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.SubRefinement

interface OnRefinementOptionSelected {
    fun onRefinementOptionSelected(refinementNavigation: RefinementNavigation)
    fun onRefinementSelected(refinement: Refinement)
    fun onBackPressedWithRefinement(navigationState: String, isMultiSelect: Boolean)
    fun onBackPressedWithOutRefinement()
    fun onSeeResults(navigationState: String, isMultiSelect: Boolean)
    fun onRefinementClear()
    fun onRefinementReset()
    fun onCategorySelected(refinement: Refinement, isMultiSelect: Boolean)
    fun hideCloseButton(){}
    fun hideBackButton(){}
    fun setPageTitle(title: String){}
}