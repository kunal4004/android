package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.RefinementsMultipleSelectionLayoutBinding
import com.awfs.coordination.databinding.RefinementsSingleSelectionLayoutBinding
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.models.dto.SubRefinement
import za.co.woolworths.financial.services.android.ui.adapters.holder.RefinementBaseViewHolder
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseFragmentListner
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected

class SubRefinementAdapter(val context: Context, val baseListner: BaseFragmentListner, val listner: OnRefinementOptionSelected, var dataList: ArrayList<RefinementSelectableItem>) : RecyclerView.Adapter<RefinementBaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefinementBaseViewHolder {
        return when (viewType) {
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
            else -> SingleSelectorHolder(
                RefinementsSingleSelectionLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RefinementBaseViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class SingleSelectorHolder(val itemBinding: RefinementsSingleSelectionLayoutBinding) : RefinementBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            itemBinding.apply {
                var item = dataList[position].item as SubRefinement
                labelSingleSelector.text = item.label
                countSingleSelector.text = item.count.toString()
                singleSelector.isChecked = dataList[position].isSelected
                root.setOnClickListener {
                    dataList.forEachIndexed { index, refinementSelectableItem ->
                        if (index == position) {
                            refinementSelectableItem.isSelected = true
                        } else if (refinementSelectableItem.type == RefinementSelectableItem.ViewType.SINGLE_SELECTOR) {
                            refinementSelectableItem.isSelected = false
                        }
                    }
                    notifyDataSetChanged()
                    baseListner.onSelectionChanged()
                }
            }
        }
    }

    inner class MultiSelectorHolder(val itemBinding: RefinementsMultipleSelectionLayoutBinding) : RefinementBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            itemBinding.apply {
                var item = dataList[position].item as SubRefinement
                labelMultiSelector.text = item.label
                countMultiSelector.text = item.count.toString()
                multiSelector.isChecked = dataList[position].isSelected
                root.setOnClickListener {
                    dataList[position].isSelected = !dataList[position].isSelected
                    notifyDataSetChanged()
                    baseListner.onSelectionChanged()
                }
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