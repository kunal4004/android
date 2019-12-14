package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.View
import android.view.animation.RotateAnimation
import kotlinx.android.synthetic.main.more_benefit_parent_item.view.*
import za.co.woolworths.financial.services.android.models.dto.account.MoreBenefit
import za.co.woolworths.financial.services.android.util.expand.ParentViewHolder

class MoreBenefitParentViewHolder(itemView: View) : ParentViewHolder(itemView) {

    fun bind(subCategoryModel: MoreBenefit, holder: MoreBenefitParentViewHolder) {
        itemView.moreBenefitsTitleTextView?.text = subCategoryModel.name
        itemView.moreBenefitsArrowImageView?.setImageResource(subCategoryModel.drawableId)
        holder.itemView.setOnClickListener {
            if (holder.isExpanded) holder.collapseView() else holder.expandView()
        }
    }

    override fun setExpanded(expanded: Boolean) {
        super.setExpanded(expanded)
        itemView.moreBenefitsIconImageView?.rotation = if (expanded) ROTATED_POSITION else INITIAL_POSITION
    }

    override fun onExpansionToggled(expanded: Boolean) {
        super.onExpansionToggled(expanded)
        val rotateAnimation: RotateAnimation? = if (expanded) { // rotate clockwise
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
        rotateAnimation?.duration = 200
        rotateAnimation?.fillAfter = true
        itemView.moreBenefitsIconImageView?.startAnimation(rotateAnimation)
    }

    companion object {
        private const val INITIAL_POSITION = 0.0f
        private const val ROTATED_POSITION = 180f
    }
}
