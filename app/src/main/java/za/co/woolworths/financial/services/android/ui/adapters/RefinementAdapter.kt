package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.refinements_multiple_selection_layout.view.*
import kotlinx.android.synthetic.main.refinements_options_layout.view.*
import kotlinx.android.synthetic.main.refinements_single_selection_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.Refinement
import za.co.woolworths.financial.services.android.models.dto.RefinementCrumb
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.RefinementBaseViewHolder
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected

class RefinementAdapter(val context: Context, val listner: OnRefinementOptionSelected, var dataList: ArrayList<RefinementSelectableItem>, refinementNavigation: RefinementNavigation) : RecyclerView.Adapter<RefinementBaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RefinementBaseViewHolder? {
        when (viewType) {
            RefinementSelectableItem.ViewType.OPTIONS.value -> {
                return OptionsHolder(LayoutInflater.from(context).inflate(R.layout.refinements_options_layout, parent, false))
            }
            RefinementSelectableItem.ViewType.SINGLE_SELECTOR.value -> {
                return SingleSelectorHolder(LayoutInflater.from(context).inflate(R.layout.refinements_single_selection_layout, parent, false))
            }
            RefinementSelectableItem.ViewType.MULTI_SELECTOR.value -> {
                return MultiSelectorHolder(LayoutInflater.from(context).inflate(R.layout.refinements_multiple_selection_layout, parent, false))
            }
        }
        return null
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RefinementBaseViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class OptionsHolder(itemView: View) : RefinementBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            var item = dataList[position].item as Refinement
            itemView.displayName.text = item.label
            itemView.refinementOptions.setOnClickListener {
                listner.onRefinementSelected(item)
            }
        }
    }

    inner class SingleSelectorHolder(itemView: View) : RefinementBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            var item = dataList[position].item
            if (item is RefinementCrumb) {
                itemView.labelSingleSelector.text = item.label
                itemView.countSingleSelector.text = item.count.toString()
                itemView.singleSelector.isChecked = item.isMultiSelectTrueForRefinementCrumbs
            } else {
                item = item as Refinement
                itemView.labelSingleSelector.text = item.label
                itemView.countSingleSelector.text = item.count.toString()
            }

            itemView.singleSelector.isChecked = dataList[position].isSelected
            itemView.setOnClickListener {
                dataList.forEachIndexed { index, refinementSelectableItem ->
                    if (index == position) {
                        refinementSelectableItem.isSelected = true
                    } else if (refinementSelectableItem.type == RefinementSelectableItem.ViewType.SINGLE_SELECTOR) {
                        refinementSelectableItem.isSelected = false
                    }
                }
                notifyDataSetChanged()
            }
        }
    }

    inner class MultiSelectorHolder(itemView: View) : RefinementBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            var item = dataList[position].item
            if (item is RefinementCrumb) {
                itemView.labelMultiSelector.text = item.label
                itemView.countMultiSelector.text = item.count.toString()
                itemView.singleSelector.isChecked = item.isMultiSelectTrueForRefinementCrumbs
            } else {
                item = item as Refinement
                itemView.labelMultiSelector.text = item.label
                itemView.countMultiSelector.text = item.count.toString()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type.value
    }
}