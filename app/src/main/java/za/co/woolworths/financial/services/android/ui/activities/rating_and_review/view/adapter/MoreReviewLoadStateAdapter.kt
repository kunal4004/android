package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.LayoutFooterMoreReviewsBinding

class MoreReviewLoadStateAdapter(
        private val retry: () -> Unit,
        private val handlePaginationError: HandlePaginationError
) : LoadStateAdapter<MoreReviewLoadStateAdapter.ReviewLoadStateViewHolder>() {

    interface HandlePaginationError {
        fun showFooterErrorMessage()
    }

    inner class ReviewLoadStateViewHolder(val itemBinding: LayoutFooterMoreReviewsBinding, val retry: () -> Unit) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bindView(loadState: LoadState) {
            itemBinding.apply {
                if (loadState is LoadState.Loading) {
                    pbFooterProgress.visibility = View.VISIBLE
                } else {
                    pbFooterProgress.visibility = View.GONE
                    handlePaginationError.showFooterErrorMessage()
                }
            }
        }
    }

    override fun onBindViewHolder(holder: MoreReviewLoadStateAdapter.ReviewLoadStateViewHolder, loadState: LoadState) {
        holder.bindView(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState):
            MoreReviewLoadStateAdapter.ReviewLoadStateViewHolder {
        return ReviewLoadStateViewHolder(
            LayoutFooterMoreReviewsBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            retry
        )
    }
}
