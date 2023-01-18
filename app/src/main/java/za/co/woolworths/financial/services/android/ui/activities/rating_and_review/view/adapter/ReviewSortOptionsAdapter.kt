package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ReviewSortSelectionLayoutBinding
import za.co.woolworths.financial.services.android.models.dto.SortOption

class ReviewSortOptionsAdapter(
    val context: Context,
    var sortOptions: MutableList<SortOption>,
    var listner: OnSortOptionSelected
) : RecyclerView.Adapter<ReviewSortOptionsAdapter.ReviewSortOptionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewSortOptionViewHolder {
        return ReviewSortOptionViewHolder(
            ReviewSortSelectionLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return sortOptions.size
    }

    override fun onBindViewHolder(holder: ReviewSortOptionViewHolder, position: Int) {
        holder.tvSortOption.text = sortOptions[position].label
        holder.rbsortSelector.isChecked = sortOptions[position].selected
        holder.view.setOnClickListener {
            sortOptions[position].selected = true
            listner.onSortOptionSelected(sortOptions[position])
            notifyDataSetChanged()
        }
    }

    inner class ReviewSortOptionViewHolder(val itemBinding: ReviewSortSelectionLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        val tvSortOption = itemBinding.sortOptionType
        val view = itemBinding.root
        val rbsortSelector = itemBinding.sortSelector
    }

    interface OnSortOptionSelected {
        fun onSortOptionSelected(sortOption: SortOption)
    }
}