package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.SortOpitionsItemBinding
import za.co.woolworths.financial.services.android.models.dto.SortOption

class SortOptionsAdapter(
    val context: Context,
    var sortOptions: ArrayList<SortOption>,
    var listner: OnSortOptionSelected
) : RecyclerView.Adapter<SortOptionsAdapter.SortOptionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortOptionViewHolder {
        return SortOptionViewHolder(
            SortOpitionsItemBinding.inflate(LayoutInflater.from(context), parent, false)
        )
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

    inner class SortOptionViewHolder(val itemBinding: SortOpitionsItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        val tvSortOption = itemBinding.sortOptionType
        val view = itemBinding.root
        val rbsortSelector = itemBinding.sortSelector
    }

    interface OnSortOptionSelected {
        fun onSortOptionSelected(sortOption: SortOption)
    }
}