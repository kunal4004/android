package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ReviewRefineSelectionLayoutBinding
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Refinements
import za.co.woolworths.financial.services.android.ui.views.WTextView

class ReviewRefineOptionsAdapter (
    val context: Context,
    var refinementOptions: MutableList<Refinements>
) : RecyclerView.Adapter<ReviewRefineOptionsAdapter.ReviewRefineOptionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewRefineOptionViewHolder {
        return ReviewRefineOptionViewHolder(
            ReviewRefineSelectionLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return refinementOptions.size
    }

    override fun onBindViewHolder(holder: ReviewRefineOptionViewHolder, position: Int) {
        holder.tvRefineOption.text = refinementOptions[position].displayName
        holder.cbRefineSelector.isChecked = refinementOptions[position].selected
        holder.view.setOnClickListener {
            refinementOptions[position].selected = !refinementOptions[position].selected
            notifyDataSetChanged()
        }
    }

    inner class ReviewRefineOptionViewHolder(val itemBinding: ReviewRefineSelectionLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        val tvRefineOption: TextView = itemBinding.tvRefineOption
        val view = itemView
        val cbRefineSelector: CheckBox = itemBinding.cbRefineSelector
    }

    fun clearRefinement() {
        refinementOptions.forEach {
            it.selected = false
        }
        notifyDataSetChanged()
    }

    fun getRefineOption(): String? {
        var refineString: String? = null
        refinementOptions.forEach {
            if(it.selected) {
                refineString = if (refineString == null)
                    it.navigationState
                else
                    refineString + "," + it.navigationState
            }
        }
       return refineString
    }
}