package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.View
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import kotlinx.android.synthetic.main.more_benefit_child_item.view.*

class MoreBenefitsChildViewHolder(itemView: View) : ChildViewHolder(itemView) {

    companion object {
        private const val splitBenefitSymbol = "||"
    }

    fun setMoreBenefitsChildItem(name: String) {
        if (name.contains(splitBenefitSymbol)) {
            val benefitItems = name.split(splitBenefitSymbol)
        }
        itemView.moreBenefitDescriptionTextView?.text = name

    }
}
