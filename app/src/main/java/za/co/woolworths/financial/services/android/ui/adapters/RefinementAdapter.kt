package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
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
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseFragmentListner
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected

class RefinementAdapter(val context: Context, val baseListner: BaseFragmentListner, val listner: OnRefinementOptionSelected, var dataList: ArrayList<RefinementSelectableItem>, refinementNavigation: RefinementNavigation) : RecyclerView.Adapter<RefinementBaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefinementBaseViewHolder {
        return when (viewType) {
            RefinementSelectableItem.ViewType.OPTIONS.value -> {
                OptionsHolder(LayoutInflater.from(context).inflate(R.layout.refinements_options_layout, parent, false))
            }
            RefinementSelectableItem.ViewType.SINGLE_SELECTOR.value -> {
                SingleSelectorHolder(LayoutInflater.from(context).inflate(R.layout.refinements_single_selection_layout, parent, false))
            }
            RefinementSelectableItem.ViewType.MULTI_SELECTOR.value -> {
                MultiSelectorHolder(LayoutInflater.from(context).inflate(R.layout.refinements_multiple_selection_layout, parent, false))
            }
            else -> OptionsHolder(LayoutInflater.from(context).inflate(R.layout.refinements_options_layout, parent, false))
        }
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
                itemView.singleSelector.isChecked = dataList[position].isSelected
            } else {
                item = item as Refinement
                itemView.labelSingleSelector.text = item.label
                itemView.countSingleSelector.text = item.count.toString()
            }

            itemView.singleSelector.isChecked = dataList[position].isSelected
            itemView.setOnClickListener {
                dataList.forEachIndexed { index, refinementSelectableItem ->
                    if (index == position) {
                        refinementSelectableItem.isSelected = if (item is RefinementCrumb) !refinementSelectableItem.isSelected else true
                    } else if (refinementSelectableItem.type == RefinementSelectableItem.ViewType.SINGLE_SELECTOR) {
                        refinementSelectableItem.isSelected = false
                    }
                }
                notifyDataSetChanged()
                baseListner.onSelectionChanged()
            }
        }
    }

    inner class MultiSelectorHolder(itemView: View) : RefinementBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            var item = dataList[position].item
            if (item is RefinementCrumb) {
                itemView.labelMultiSelector.text = item.label
                itemView.countMultiSelector.text = item.count.toString()
                itemView.multiSelector.isChecked = dataList[position].isSelected
            } else {
                item = item as Refinement
                itemView.labelMultiSelector.text = item.label
                itemView.countMultiSelector.text = item.count.toString()
                itemView.multiSelector.isChecked = dataList[position].isSelected
            }

            itemView.setOnClickListener {
                dataList[position].isSelected = !dataList[position].isSelected
                notifyDataSetChanged()
                baseListner.onSelectionChanged()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type.value
    }

    fun clearRefinement() {
        dataList.forEach {
            it.isSelected = false
        }
        notifyDataSetChanged()
        baseListner.onSelectionChanged()
    }
}