package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.View
import kotlinx.android.synthetic.main.more_benefit_child_item.view.*
import za.co.woolworths.financial.services.android.util.expand.ChildViewHolder

class MoreBenefitChildViewHolder(itemView: View) : ChildViewHolder(itemView) {
    fun bind(description: String?) {
        itemView.moreBenefitDescriptionTextView?.text = description
    }
}