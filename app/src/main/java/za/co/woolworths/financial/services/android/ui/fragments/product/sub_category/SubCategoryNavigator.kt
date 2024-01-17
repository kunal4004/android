package za.co.woolworths.financial.services.android.ui.fragments.product.sub_category

import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.SubCategory
import za.co.woolworths.financial.services.android.util.expand.ParentSubCategoryViewHolder

interface SubCategoryNavigator {
    fun bindSubCategoryResult(subCategoryList: List<SubCategory>, latestVersionParam: String)
    fun unhandledResponseHandler(response: Response)
    fun onFailureResponse(e: String)
    fun onLoad()
    fun onLoadComplete()
    fun onChildItemClicked(subCategory: SubCategory)
    fun noConnectionDetected()
    fun retrieveChildItem(
        parentSubCategoryViewHolder: ParentSubCategoryViewHolder,
        subCategory: SubCategory,
        adapterPosition: Int
    )
    fun onCloseIconPressed()
    fun onOrderAgainClicked()
}