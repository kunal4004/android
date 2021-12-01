package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.layout_footer_more_reviews.view.*

class MoreReviewLoadStateAdapter(
        private val retry: () -> Unit
) : LoadStateAdapter<MoreReviewLoadStateAdapter
.ReviewLoadStateViewHolder>() {

    inner class ReviewLoadStateViewHolder(
            itemView: View, val retry: () -> Unit) : RecyclerView.ViewHolder(itemView) {

        fun bindView(loadState: LoadState) {
            itemView.pbFooterProgress.isVisible = loadState is LoadState.Loading
            if (loadState is LoadState.Error) {
                itemView.linearlayout_error_footer.visibility = View.VISIBLE
                Snackbar.make(itemView.linearlayout_error_footer, R.string.failed_more_reviews, Snackbar.LENGTH_LONG).setAction(
                        R.string.retry, {
                    retry()
                }
                ).show()
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
