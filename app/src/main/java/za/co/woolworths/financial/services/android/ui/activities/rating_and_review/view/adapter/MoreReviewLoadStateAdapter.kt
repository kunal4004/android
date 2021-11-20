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

class MoreReviewLoadStateAdapter() : LoadStateAdapter<MoreReviewLoadStateAdapter
.ReviewLoadStateViewHolder>() {

    inner class ReviewLoadStateViewHolder(
            itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(loadState: LoadState) {
            if (loadState is LoadState.Loading) {
                Log.e("LoadState.Loading", "called")

                itemView.pbFooterProgress.visibility = View.VISIBLE
            } else {
                Log.e("LoadState.NotLoading", "called")
                itemView.pbFooterProgress.visibility = View.GONE
            }
        }
    }

    override fun onBindViewHolder(holder: MoreReviewLoadStateAdapter
    .ReviewLoadStateViewHolder, loadState: LoadState) {
        Log.e("onBindViewHolder", "called")
        holder.bindView(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): MoreReviewLoadStateAdapter
    .ReviewLoadStateViewHolder {
        Log.e("onCreateViewHolder", "called")
        return ReviewLoadStateViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.bottom_progress_bar, parent, false)
        )
    }
}