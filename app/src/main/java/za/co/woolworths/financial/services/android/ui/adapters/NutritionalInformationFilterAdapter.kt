package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.NutritionalInfoFilterOpitionsItemBinding
import za.co.woolworths.financial.services.android.models.dto.NutritionalInformationFilterOption

class NutritionalInformationFilterAdapter(var data: ArrayList<NutritionalInformationFilterOption>, var listener: FilterOptionSelection) : RecyclerView.Adapter<NutritionalInformationFilterAdapter.ViewHolder>() {
    interface FilterOptionSelection {
        fun onOptionSelected(selectedSortedOption: NutritionalInformationFilterOption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            NutritionalInfoFilterOpitionsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemBinding.root.setOnClickListener {
            listener.onOptionSelected(data[position])
            data.forEachIndexed { index, it ->
                it.isSelected = index == position
            }
        }
    }

    class ViewHolder(val itemBinding: NutritionalInfoFilterOpitionsItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: NutritionalInformationFilterOption) {
            itemBinding.filterOptionName.text = item.name
            itemBinding.filterSelector.isChecked = item.isSelected
        }
    }

}