package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.sort_opitions_item.view.*
import za.co.woolworths.financial.services.android.models.dto.SortOption

class ReviewSortOptionsAdapter(
    val context: Context,
    var sortOptions: MutableList<SortOption>,
    var listner: OnSortOptionSelected
) : RecyclerView.Adapter<ReviewSortOptionsAdapter.ReviewSortOptionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewSortOptionViewHolder {
        return ReviewSortOptionViewHolder(
            LayoutInflater.from(context).inflate(R.layout.review_sort_selection_layout, parent, false)
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

    inner class ReviewSortOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvSortOption = itemView.sortOptionType
        val view = itemView
        val rbsortSelector = itemView.sortSelector
    }

    interface OnSortOptionSelected {
        fun onSortOptionSelected(sortOption: SortOption)
    }
}