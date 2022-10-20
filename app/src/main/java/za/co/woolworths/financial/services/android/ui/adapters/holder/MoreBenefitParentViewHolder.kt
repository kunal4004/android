package za.co.woolworths.financial.services.android.ui.adapters.holder

import com.awfs.coordination.databinding.MoreBenefitParentItemBinding
import za.co.woolworths.financial.services.android.models.dto.account.MoreBenefit
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension.Companion.INITIAL_POSITION
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension.Companion.ROTATED_POSITION
import za.co.woolworths.financial.services.android.util.expand.ParentViewHolder

class MoreBenefitParentViewHolder(val binding: MoreBenefitParentItemBinding) : ParentViewHolder(binding.root) {

    fun bind(subCategoryModel: MoreBenefit, holder: MoreBenefitParentViewHolder) {
        binding.moreBenefitsTitleTextView?.text = subCategoryModel.name
        binding.moreBenefitsIconImageView?.setImageResource(subCategoryModel.drawableId)
        holder.binding.root.setOnClickListener {
            if (holder.isExpanded) holder.collapseView() else holder.expandView()
        }
    }

    override fun setExpanded(expanded: Boolean) {
        super.setExpanded(expanded)
        binding.moreBenefitsArrowImageView?.rotation = if (expanded) ROTATED_POSITION else INITIAL_POSITION
    }

    override fun onExpansionToggled(expanded: Boolean) {
        super.onExpansionToggled(expanded)
        AnimationUtilExtension.rotateView(expanded, binding.moreBenefitsArrowImageView)
    }

}
