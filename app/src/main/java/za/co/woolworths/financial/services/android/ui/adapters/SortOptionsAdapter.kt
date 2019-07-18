package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.SortOption
import kotlinx.android.synthetic.main.sort_opitions_item.view.*

class SortOptionsAdapter(val context: Context, var sortOptions: ArrayList<SortOption>, var listner: OnSortOptionSelected) : RecyclerView.Adapter<SortOptionsAdapter.SortOptionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortOptionViewHolder {
        return SortOptionViewHolder(LayoutInflater.from(context).inflate(R.layout.sort_opitions_item, parent, false))
    }

    override fun getItemCount(): Int {
        return sortOptions.size
    }

    override fun onBindViewHolder(holder: SortOptionViewHolder, position: Int) {
        holder.tvSortOption.text = sortOptions[position].label
        holder.rbsortSelector.isChecked = sortOptions[position].selected
        holder.view.setOnClickListener {
            sortOptions[position].selected = true
            listner.onSortOptionSelected(sortOptions[position])
            notifyDataSetChanged()
        }
    }

    inner class SortOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvSortOption = itemView.sortOptionType
        val view = itemView
        val rbsortSelector = itemView.sortSelector
    }

    interface OnSortOptionSelected {
        fun onSortOptionSelected(sortOption: SortOption)
    }
}