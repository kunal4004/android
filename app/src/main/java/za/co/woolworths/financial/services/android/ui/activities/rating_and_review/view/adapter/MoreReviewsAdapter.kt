package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews

class MoreReviewsAdapter : PagingDataAdapter<Reviews, MoreReviewsAdapter.ViewHolder>(DataDifferntiator) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.title?.let { Log.e("title_is" , it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.review_row_layout, parent, false)
        )
    }

    object DataDifferntiator : DiffUtil.ItemCallback<Reviews>() {
        override fun areItemsTheSame(oldReview: Reviews, newReview: Reviews): Boolean {
            return oldReview.id == newReview.id
        }

        override fun areContentsTheSame(oldItem: Reviews, newItem: Reviews): Boolean {
            return oldItem == newItem
        }
    }
}