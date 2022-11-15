package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.report_item_cell.view.*

class ReportReviewsAdapter (
        private var reportReviewList: List<String>,
        var reportItemClick: ReportItemClick):
        RecyclerView.Adapter<ReportReviewsAdapter.ViewHolder>() {

    private var count = 0
   inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bindItems(reportReview: String) {
            itemView.checkBox.text = reportReview
            itemView.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    count ++
                } else {
                    if (count>=0) {
                        count--
                    }
                }
                reportItemClick.reportItemClicked(reportReview, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.report_item_cell, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(reportReviewList.get(position))
    }

    override fun getItemCount(): Int {
        return reportReviewList.size
    }

    fun getAllCheckBoxCount() = count

    fun getSelectedCheckbox(){

    }

    interface ReportItemClick {
        fun reportItemClicked(reportItem: String, isChecked: Boolean)
    }
}

