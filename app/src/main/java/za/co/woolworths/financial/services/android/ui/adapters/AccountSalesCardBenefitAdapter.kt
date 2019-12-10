package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.awfs.coordination.R
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import za.co.woolworths.financial.services.android.models.dto.account.MoreBenefits
import za.co.woolworths.financial.services.android.ui.adapters.holder.MoreBenefitsChildViewHolder
import za.co.woolworths.financial.services.android.ui.adapters.holder.MoreBenefitsParentViewHolder

class AccountSalesCardBenefitAdapter(groups: MutableList<MoreBenefits>) : ExpandableRecyclerViewAdapter<MoreBenefitsParentViewHolder,
        MoreBenefitsChildViewHolder>(groups) {

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): MoreBenefitsParentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.more_benefit_parent_item, parent, false)
        return MoreBenefitsParentViewHolder(view)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): MoreBenefitsChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.more_benefit_child_item, parent, false)
        return MoreBenefitsChildViewHolder(view)
    }

    override fun onBindChildViewHolder(holder: MoreBenefitsChildViewHolder, flatPosition: Int, group: ExpandableGroup<*>, childIndex: Int) {
        val benefitItems = (group as? MoreBenefits)?.items?.get(childIndex)
        benefitItems?.description?.let { name -> holder.setMoreBenefitsChildItem(name) }
    }

    override fun onBindGroupViewHolder(holder: MoreBenefitsParentViewHolder, flatPosition: Int, group: ExpandableGroup<*>) {
        (group as? MoreBenefits)?.let { moreBenefit -> holder.setBenefitParentItem(moreBenefit) }
    }
}