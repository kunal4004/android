package za.co.woolworths.financial.services.android.util.expand

import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.SubCategoryHeaderViewBinding
import za.co.woolworths.financial.services.android.util.ImageManager.Companion.setPictureCenterInside

class HeaderViewHolder(val itemBinding: SubCategoryHeaderViewBinding) : RecyclerView.ViewHolder(itemBinding.root) {
    fun bind(subCategoryModel: SubCategoryModel) {
        itemBinding.tvCategoryName.setText(subCategoryModel.name)
        setPictureCenterInside(itemBinding.imProductCategory, subCategoryModel.imageUrl)
    }
}