package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R

import kotlinx.android.synthetic.main.layout_footer_more_reviews.view.*

class MoreReviewLoadStateAdapter(
        private val retry: () -> Unit,
        private val handlePaginationError: HandlePaginationError

) : LoadStateAdapter<MoreReviewLoadStateAdapter
.ReviewLoadStateViewHolder>() {

    interface HandlePaginationError {
        fun showFooterErrorMessage()
    }

    inner class ReviewLoadStateViewHolder(
            itemView: View, val retry: () -> Unit) : RecyclerView.ViewHolder(itemView) {

        fun bindView(loadState: LoadState) {
            if (loadState is LoadState.Loading) {
                itemView.pbFooterProgress.visibility = View.VISIBLE
            } else {
                itemView.pbFooterProgress.visibility = View.GONE
                handlePaginationError.showFooterErrorMessage()
            }
        }
    }

    override fun onBindViewHolder(holder: MoreReviewLoadStateAdapter
    .ReviewLoadStateViewHolder, loadState: LoadState) {
        holder.bindView(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState):
            MoreReviewLoadStateAdapter.ReviewLoadStateViewHolder {
        return ReviewLoadStateViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.layout_footer_more_reviews, parent, false),
                retry
        )
    }
}
