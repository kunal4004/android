package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.View
import kotlinx.android.synthetic.main.more_benefit_parent_item.view.*
import za.co.woolworths.financial.services.android.models.dto.account.MoreBenefit
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension.Companion.INITIAL_POSITION
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension.Companion.ROTATED_POSITION
import za.co.woolworths.financial.services.android.util.expand.ParentViewHolder

class MoreBenefitParentViewHolder(itemView: View) : ParentViewHolder(itemView) {

    fun bind(subCategoryModel: MoreBenefit, holder: MoreBenefitParentViewHolder) {
        itemView.moreBenefitsTitleTextView?.text = subCategoryModel.name
        itemView.moreBenefitsIconImageView?.setImageResource(subCategoryModel.drawableId)
        holder.itemView.setOnClickListener {
            if (holder.isExpanded) holder.collapseView() else holder.expandView()
        }
    }

    override fun setExpanded(expanded: Boolean) {
        super.setExpanded(expanded)
        itemView.moreBenefitsArrowImageView?.rotation = if (expanded) ROTATED_POSITION else INITIAL_POSITION
    }

    override fun onExpansionToggled(expanded: Boolean) {
        super.onExpansionToggled(expanded)
        AnimationUtilExtension.rotateView(expanded, itemView.moreBenefitsArrowImageView)
    }

}
