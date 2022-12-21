package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.awfs.coordination.databinding.MoreBenefitChildItemBinding
import com.awfs.coordination.databinding.MoreBenefitParentItemBinding
import za.co.woolworths.financial.services.android.models.dto.account.MoreBenefit
import za.co.woolworths.financial.services.android.ui.adapters.holder.MoreBenefitChildViewHolder
import za.co.woolworths.financial.services.android.ui.adapters.holder.MoreBenefitParentViewHolder
import za.co.woolworths.financial.services.android.util.expand.ExpandableAdapter
import za.co.woolworths.financial.services.android.util.expand.ParentListItem

class MoreBenefitAdapter(parentItemList: List<ParentListItem?>?) : ExpandableAdapter<MoreBenefitParentViewHolder?, MoreBenefitChildViewHolder?>(parentItemList) {

    override fun onCreateParentViewHolder(parentViewGroup: ViewGroup, viewType: Int): MoreBenefitParentViewHolder {
        return MoreBenefitParentViewHolder(
            MoreBenefitParentItemBinding.inflate(LayoutInflater.from(parentViewGroup.context), parentViewGroup, false)
        )
    }

    override fun onCreateChildViewHolder(childViewGroup: ViewGroup, viewType: Int): MoreBenefitChildViewHolder {
        return MoreBenefitChildViewHolder(
            MoreBenefitChildItemBinding.inflate(LayoutInflater.from(childViewGroup.context), childViewGroup, false)
        )
    }

    override fun onBindParentViewHolder(holder: MoreBenefitParentViewHolder?, position: Int, parentListItem: ParentListItem?) {
        val benefitListItem = parentListItem as? MoreBenefit
        benefitListItem?.let { headerItem -> holder?.bind(headerItem, holder) }
    }

    override fun onBindChildViewHolder(childHolder: MoreBenefitChildViewHolder?, position: Int, childListItem: Any?) {
        val childItem = childListItem as? String
        childHolder?.bind(childItem)
    }
}
