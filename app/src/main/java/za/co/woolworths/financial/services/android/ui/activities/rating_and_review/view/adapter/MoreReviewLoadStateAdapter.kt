package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bottom_progress_bar.view.*
import kotlinx.android.synthetic.main.bottom_progress_bar.view.pbFooterProgress
import kotlinx.android.synthetic.main.layout_footer_more_reviews.view.*

class MoreReviewLoadStateAdapter(
        private val retry: () -> Unit
) : LoadStateAdapter<MoreReviewLoadStateAdapter
.ReviewLoadStateViewHolder>() {

    inner class ReviewLoadStateViewHolder(
            itemView: View, val retry: () -> Unit) : RecyclerView.ViewHolder(itemView) {

        fun bindView(loadState: LoadState) {
            Log.e("ReviewLoadStateViewHolder :", "called")
            if (loadState is LoadState.Loading) {
                itemView.pbFooterProgress.visibility = View.VISIBLE
            } else if (loadState is LoadState.Error){
                Log.e("ReviewLoadStateViewHolder_Error  :", "called")
                itemView.pbFooterProgress.visibility = View.GONE
                itemView.linearlayout_error_footer.visibility = View.VISIBLE
            }
            itemView.linearlayout_error_footer.txt_retry.setOnClickListener {
                retry()
            }
        }
    }

    override fun onBindViewHolder(holder: MoreReviewLoadStateAdapter
    .ReviewLoadStateViewHolder, loadState: LoadState) {
        Log.e("onCreateViewHolder :", "called")
        holder.bindView(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState):
            MoreReviewLoadStateAdapter.ReviewLoadStateViewHolder {
        Log.e("onCreateViewHolder :", "called")
        return ReviewLoadStateViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.layout_footer_more_reviews, parent, false),
                retry
        )
    }
}