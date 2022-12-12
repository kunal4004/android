package za.co.woolworths.financial.services.android.util.expand

import com.awfs.coordination.databinding.SubCategoryChildViewBinding
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryNavigator

class SubCategoryViewHolder(val itemBinding: SubCategoryChildViewBinding) : ChildViewHolder(itemBinding.root) {

    fun bind(subCategoryNavigator: SubCategoryNavigator, subCategoryChild: SubCategoryChild) {
        itemBinding.tvChildItemName.setText(subCategoryChild.subCategory.categoryName)
        listener(subCategoryNavigator, subCategoryChild)
    }

    fun listener(subCategoryNavigator: SubCategoryNavigator, subCategoryChild: SubCategoryChild) {
        itemBinding.tvChildItemName.setOnClickListener {
            subCategoryNavigator.onChildItemClicked(
                subCategoryChild.subCategory
            )
        }
    }
}