package za.co.woolworths.financial.services.android.util.expand

import android.view.View
import android.view.animation.RotateAnimation
import kotlinx.android.synthetic.main.sub_category_parent_view.view.*
import za.co.woolworths.financial.services.android.util.ImageManager.Companion.setPictureWithoutPlaceHolder


class ParentSubCategoryViewHolder(itemView: View) : ParentViewHolder(itemView) {

    fun bind(subCategoryModel: SubCategoryModel) {
        itemView.carbonGroupText?.setText(subCategoryModel.name)
        arrowVisibility(subCategoryModel)
        retrieveChildVisibility(subCategoryModel)
        setNewBadgeImage(subCategoryModel)
    }

    private fun setNewBadgeImage(subCategoryModel: SubCategoryModel) {
        if (subCategoryModel.imageUrl != null) {
            itemView.imgBadge?.visibility = View.VISIBLE
            setPictureWithoutPlaceHolder(itemView.imgBadge, subCategoryModel.imageUrl)
        } else {
            itemView.imgBadge?.visibility = View.GONE
        }
    }

    private fun arrowVisibility(subCategoryModel: SubCategoryModel) {
        itemView.llLoadChild?.visibility =
            if (subCategoryModel.subCategory.hasChildren) View.VISIBLE else View.INVISIBLE
    }

    private fun retrieveChildVisibility(subCategoryModel: SubCategoryModel) {
        val progressBarIsVisible = subCategoryModel.subCategory.singleProductItemIsLoading
        itemView.pbLoadChildItem?.visibility = if (progressBarIsVisible) View.VISIBLE else View.GONE
        itemView.carbonGroupExpandedIndicator?.visibility =
            if (progressBarIsVisible) View.GONE else View.VISIBLE
    }

    fun retrieveChildVisibility(visible: Boolean) {
        itemView.pbLoadChildItem?.visibility = if (visible) View.VISIBLE else View.GONE
        itemView.carbonGroupExpandedIndicator?.visibility = if (visible) View.GONE else View.VISIBLE
    }

    override fun setExpanded(expanded: Boolean) {
        super.setExpanded(expanded)
        if (expanded) {
            itemView.carbonGroupExpandedIndicator?.rotation = ROTATED_POSITION
        } else {
            itemView.carbonGroupExpandedIndicator?.rotation = INITIAL_POSITION
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
        itemView.carbonGroupExpandedIndicator?.startAnimation(rotateAnimation)
    }

    companion object {
        private const val INITIAL_POSITION = 0.0f
        private const val ROTATED_POSITION = 180f
    }

}