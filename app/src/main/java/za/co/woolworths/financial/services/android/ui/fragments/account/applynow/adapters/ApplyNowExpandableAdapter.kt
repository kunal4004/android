package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.awfs.coordination.databinding.MoreBenefitChildItemBinding
import com.awfs.coordination.databinding.MoreBenefitParentItemBinding
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ChildrenItems
import za.co.woolworths.financial.services.android.ui.adapters.holder.MoreBenefitChildViewHolder
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.loadSvg
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.expand.ExpandableAdapter
import za.co.woolworths.financial.services.android.util.expand.ParentListItem
import za.co.woolworths.financial.services.android.util.expand.ParentViewHolder

class ApplyNowExpandableAdapter(parentItemList: List<ParentListItem?>?) : ExpandableAdapter<ApplyNowExpandableAdapter.ApplyNowBenefitsParentViewHolder?, MoreBenefitChildViewHolder?>(parentItemList) {

    override fun onCreateParentViewHolder(parentViewGroup: ViewGroup, viewType: Int): ApplyNowBenefitsParentViewHolder {
        return ApplyNowBenefitsParentViewHolder(
            MoreBenefitParentItemBinding.inflate(LayoutInflater.from(parentViewGroup.context), parentViewGroup, false)
        )
    }

    override fun onCreateChildViewHolder(childViewGroup: ViewGroup, viewType: Int): MoreBenefitChildViewHolder {
        return MoreBenefitChildViewHolder(
            MoreBenefitChildItemBinding.inflate(LayoutInflater.from(childViewGroup.context), childViewGroup, false)
        )
    }

    override fun onBindParentViewHolder(holder: ApplyNowBenefitsParentViewHolder?, position: Int, parentListItem: ParentListItem?) {
        val benefitListItem = parentListItem as? ChildrenItems
        benefitListItem?.let { headerItem -> holder?.bind(headerItem, holder) }
    }

    override fun onBindChildViewHolder(childHolder: MoreBenefitChildViewHolder?, position: Int, childListItem: Any?) {
        val childItem = childListItem as? String
        childHolder?.bind(childItem)
    }

    inner class ApplyNowBenefitsParentViewHolder(val binding: MoreBenefitParentItemBinding) : ParentViewHolder(binding.root) {

        fun bind(subCategoryModel: ChildrenItems, holder: ApplyNowBenefitsParentViewHolder) {
            with(MoreBenefitParentItemBinding.bind(itemView)){
                moreBenefitsTitleTextView.text = subCategoryModel.title
                moreBenefitsIconImageView.loadSvg(subCategoryModel.imageUrl)
                holder.itemView.setOnClickListener {
                    if (holder.isExpanded) holder.collapseView() else holder.expandView()
                }
            }
        }

        override fun setExpanded(expanded: Boolean) {
            super.setExpanded(expanded)
            binding.moreBenefitsArrowImageView?.rotation = if (expanded) AnimationUtilExtension.ROTATED_POSITION else AnimationUtilExtension.INITIAL_POSITION
        }

        override fun onExpansionToggled(expanded: Boolean) {
            super.onExpansionToggled(expanded)
            AnimationUtilExtension.rotateView(expanded, binding.moreBenefitsArrowImageView)
        }

    }
}