package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.RefinementsMultipleSelectionLayoutBinding
import com.awfs.coordination.databinding.RefinementsOptionsLayoutBinding
import com.awfs.coordination.databinding.RefinementsSingleSelectionLayoutBinding
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
                OptionsHolder(
                    RefinementsOptionsLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            RefinementSelectableItem.ViewType.SINGLE_SELECTOR.value -> {
                SingleSelectorHolder(
                    RefinementsSingleSelectionLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            RefinementSelectableItem.ViewType.MULTI_SELECTOR.value -> {
                MultiSelectorHolder(
                    RefinementsMultipleSelectionLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            RefinementSelectableItem.ViewType.CATEGORY.value -> {
                CategoryHolder(
                    RefinementsOptionsLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            else -> OptionsHolder(
                RefinementsOptionsLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RefinementBaseViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class OptionsHolder(val itemBinding: RefinementsOptionsLayoutBinding) : RefinementBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            var item = dataList[position].item as Refinement
            itemBinding.displayName.text = item.label
            itemBinding.refinementOptions.setOnClickListener {
                listner.onRefinementSelected(item)
            }
        }
    }

    inner class SingleSelectorHolder(val itemBinding: RefinementsSingleSelectionLayoutBinding) : RefinementBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            var item = dataList[position].item
            if (item is RefinementCrumb) {
                itemBinding.labelSingleSelector.text = item.label
                itemBinding.countSingleSelector.text = item.count.toString()
                itemBinding.singleSelector.isChecked = dataList[position].isSelected
            } else {
                item = item as Refinement
                itemBinding.labelSingleSelector.text = item.label
                itemBinding.countSingleSelector.text = item.count.toString()
            }

            itemBinding.singleSelector.isChecked = dataList[position].isSelected
            itemBinding.root.setOnClickListener {
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

    inner class MultiSelectorHolder(val itemBinding: RefinementsMultipleSelectionLayoutBinding) : RefinementBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            var item = dataList[position].item
            if (item is RefinementCrumb) {
                itemBinding.labelMultiSelector.text = item.label
                itemBinding.countMultiSelector.text = item.count.toString()
                itemBinding.multiSelector.isChecked = dataList[position].isSelected
            } else {
                item = item as Refinement
                itemBinding.labelMultiSelector.text = item.label
                itemBinding.countMultiSelector.text = item.count.toString()
                itemBinding.multiSelector.isChecked = dataList[position].isSelected
            }

            itemBinding.root.setOnClickListener {
                dataList[position].isSelected = !dataList[position].isSelected
                notifyDataSetChanged()
                baseListner.onSelectionChanged()
            }
        }
    }

    inner class CategoryHolder(val itemBinding: RefinementsOptionsLayoutBinding) : RefinementBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            val item = dataList[position].item as Refinement
            itemBinding.displayName.text = item.label
            itemBinding.rightArrow.visibility = View.INVISIBLE
            itemBinding.refinementOptions.setOnClickListener {
                listner.onCategorySelected(item, item.multiSelect)
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