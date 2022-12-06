package za.co.woolworths.financial.services.android.util.expand

import android.view.View
import android.view.animation.RotateAnimation
import com.awfs.coordination.databinding.SubCategoryParentViewBinding
import za.co.woolworths.financial.services.android.util.ImageManager.Companion.setPictureWithoutPlaceHolder

class ParentSubCategoryViewHolder(val itemBinding: SubCategoryParentViewBinding) : ParentViewHolder(itemBinding.root) {

    fun bind(subCategoryModel: SubCategoryModel) {
        itemBinding.carbonGroupText?.setText(subCategoryModel.name)
        arrowVisibility(subCategoryModel)
        retrieveChildVisibility(subCategoryModel)
        setNewBadgeImage(subCategoryModel)
    }

    private fun setNewBadgeImage(subCategoryModel: SubCategoryModel) {
        if (subCategoryModel.imageUrl != null) {
            itemBinding.imgBadge?.visibility = View.VISIBLE
            setPictureWithoutPlaceHolder(itemBinding.imgBadge, subCategoryModel.imageUrl)
        } else {
            itemBinding.imgBadge?.visibility = View.GONE
        }
    }

    private fun arrowVisibility(subCategoryModel: SubCategoryModel) {
        itemBinding.llLoadChild?.visibility =
            if (subCategoryModel.subCategory.hasChildren) View.VISIBLE else View.INVISIBLE
    }

    private fun retrieveChildVisibility(subCategoryModel: SubCategoryModel) {
        val progressBarIsVisible = subCategoryModel.subCategory.singleProductItemIsLoading
        itemBinding.pbLoadChildItem?.visibility = if (progressBarIsVisible) View.VISIBLE else View.GONE
        itemBinding.carbonGroupExpandedIndicator?.visibility =
            if (progressBarIsVisible) View.GONE else View.VISIBLE
    }

    fun retrieveChildVisibility(visible: Boolean) {
        itemBinding.pbLoadChildItem?.visibility = if (visible) View.VISIBLE else View.GONE
        itemBinding.carbonGroupExpandedIndicator?.visibility = if (visible) View.GONE else View.VISIBLE
    }

    override fun setExpanded(expanded: Boolean) {
        super.isRowExpanded = expanded
        if (expanded) {
            itemBinding.carbonGroupExpandedIndicator?.rotation = ROTATED_POSITION
        } else {
            itemBinding.carbonGroupExpandedIndicator?.rotation = INITIAL_POSITION
        }
    }

    override fun onExpansionToggled(expanded: Boolean) {
        super.onExpansionToggled(expanded)
        val rotateAnimation: RotateAnimation = if (expanded) { // rotate clockwise
            RotateAnimation(ROTATED_POSITION,
                INITIAL_POSITION,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f)
        } else { // rotate counterclockwise
            RotateAnimation(-1 * ROTATED_POSITION,
                INITIAL_POSITION,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f)
        }
        rotateAnimation.duration = 200
        rotateAnimation.fillAfter = true
        itemBinding.carbonGroupExpandedIndicator?.startAnimation(rotateAnimation)
    }

    companion object {
        private const val INITIAL_POSITION = 0.0f
        private const val ROTATED_POSITION = 180f
    }

}