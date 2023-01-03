package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ReportItemCellBinding

class ReportReviewsAdapter (
        private var reportReviewList: List<String>,
        var reportItemClick: ReportItemClick):
        RecyclerView.Adapter<ReportReviewsAdapter.ViewHolder>() {

    private var count = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ReportItemCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
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

    inner class ViewHolder(val itemBinding: ReportItemCellBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItems(reportReview: String) {
            itemBinding.checkBox.text = reportReview
            itemBinding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
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
}

