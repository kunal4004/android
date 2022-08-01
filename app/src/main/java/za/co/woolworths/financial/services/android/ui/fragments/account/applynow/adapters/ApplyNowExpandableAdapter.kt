package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.MoreBenefitParentItemBinding
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.more_benefit_parent_item.view.*
import za.co.woolworths.financial.services.android.models.dto.account.MoreBenefit
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ChildrenItems
import za.co.woolworths.financial.services.android.ui.adapters.holder.MoreBenefitChildViewHolder
import za.co.woolworths.financial.services.android.ui.adapters.holder.MoreBenefitParentViewHolder
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.loadSvg
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.expand.ExpandableAdapter
import za.co.woolworths.financial.services.android.util.expand.ParentListItem
import za.co.woolworths.financial.services.android.util.expand.ParentViewHolder

class ApplyNowExpandableAdapter(parentItemList: List<ParentListItem?>?) : ExpandableAdapter<ApplyNowExpandableAdapter.ApplyNowBenefitsParentViewHolder?, MoreBenefitChildViewHolder?>(parentItemList) {

    override fun onCreateParentViewHolder(parentViewGroup: ViewGroup, viewType: Int): ApplyNowBenefitsParentViewHolder {
        val parentCategoryView = LayoutInflater.from(parentViewGroup.context).inflate(R.layout.more_benefit_parent_item, parentViewGroup, false)
        return ApplyNowBenefitsParentViewHolder(parentCategoryView)
    }

    override fun onCreateChildViewHolder(childViewGroup: ViewGroup, viewType: Int): MoreBenefitChildViewHolder {
        val childView = LayoutInflater.from(childViewGroup.context).inflate(R.layout.more_benefit_child_item, childViewGroup, false)
        return MoreBenefitChildViewHolder(childView)
    }

    override fun onBindParentViewHolder(holder: ApplyNowBenefitsParentViewHolder?, position: Int, parentListItem: ParentListItem?) {
        val benefitListItem = parentListItem as? ChildrenItems
        benefitListItem?.let { headerItem -> holder?.bind(headerItem, holder) }
    }

    override fun onBindChildViewHolder(childHolder: MoreBenefitChildViewHolder?, position: Int, childListItem: Any?) {
        val childItem = childListItem as? String
        childHolder?.bind(childItem)
    }

    inner class ApplyNowBenefitsParentViewHolder(itemView: View) : ParentViewHolder(itemView) {

        fun bind(subCategoryModel: ChildrenItems, holder: ApplyNowBenefitsParentViewHolder) {
            with(MoreBenefitParentItemBinding.bind(itemView)){
                moreBenefitsTitleTextView.text = subCategoryModel.title
                holder.itemView.setOnClickListener {
                    if (holder.isExpanded) holder.collapseView() else holder.expandView()
                    moreBenefitsIconImageView.loadSvg(subCategoryModel.imageUrl)

                }
            }
        }

        override fun setExpanded(expanded: Boolean) {
            super.setExpanded(expanded)
            itemView.moreBenefitsArrowImageView?.rotation = if (expanded) AnimationUtilExtension.ROTATED_POSITION else AnimationUtilExtension.INITIAL_POSITION
        }

        override fun onExpansionToggled(expanded: Boolean) {
            super.onExpansionToggled(expanded)
            AnimationUtilExtension.rotateView(expanded, itemView.moreBenefitsArrowImageView)
        }

    }
}