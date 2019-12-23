package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.nutritional_info_filter_opitions_item.view.*
import za.co.woolworths.financial.services.android.models.dto.NutritionalInformationFilterOption

class NutritionalInformationFilterAdapter(var data: ArrayList<NutritionalInformationFilterOption>, var listener: FilterOptionSelection) : RecyclerView.Adapter<NutritionalInformationFilterAdapter.ViewHolder>() {
    interface FilterOptionSelection {
        fun onOptionSelected(filterOption: NutritionalInformationFilterOption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.nutritional_info_filter_opitions_item, parent, false)
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener {
            listener.onOptionSelected(data[position])
            data.forEachIndexed { index, it ->
                it.isSelected = index == position
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: NutritionalInformationFilterOption) {
            itemView.filterOptionName.text = item.name
            itemView.filterSelector.isChecked = item.isSelected
        }
    }

}