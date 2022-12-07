package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.HeaderMoreReviewRecyclerViewBinding
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingDistribution
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.ReviewStatistics
import za.co.woolworths.financial.services.android.util.Utils

class MoreReviewHeaderAdapter(var reviewStatistics: List<ReviewStatistics>,
                              val sortAndRefineListener: SortAndRefineListener,
                              var totalPage: Int)
    : RecyclerView.Adapter<MoreReviewHeaderAdapter.HeaderViewHolder>() {

    interface SortAndRefineListener {
        fun openRefineDrawer()
        fun openSortDrawer()
    }

    fun setReviewStatics(reviewStatisticsList: List<ReviewStatistics>) {
        this.reviewStatistics = reviewStatisticsList
    }

    fun setReviewTotalCounts(totalPage: Int){
        this.totalPage = totalPage
    }

    inner class HeaderViewHolder(val itemBinding: HeaderMoreReviewRecyclerViewBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bindView() {
            itemBinding.apply {
                ratingstatics.closeTop.visibility = View.GONE
                ratingstatics.ratingDetails.text = root.context.getString(R.string.customer_reviews)
                ratingstatics.pdpratings.root.visibility = View.VISIBLE
                sortAndRefine.root.visibility = View.VISIBLE
                sortAndRefine.refineProducts.setOnClickListener({ sortAndRefineListener.openRefineDrawer() })
                sortAndRefine.sortProducts.setOnClickListener( { sortAndRefineListener.openSortDrawer() })


                if (reviewStatistics.isEmpty()) {
                    return
                }
                reviewStatistics[0].apply {
                    val recommend= recommendedPercentage.split("%")
                    if (recommend.size == 2) {
                        ratingstatics.tvRecommendPercent.text = "${recommend[0]}% "
                        ratingstatics.tvRecommendTxtValue.text = recommend[1]
                    }
                    ratingstatics.pdpratings.apply {
                        ratingBarTop.visibility = View.VISIBLE
                        tvTotalReviews.visibility = View.VISIBLE
                        ratingBarTop.rating = averageRating
                        tvTotalReviews.text = root.resources.getQuantityString(R.plurals.no_review, reviewCount, reviewCount)
                        tvTotalReviews.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    }
                    ratingstatics.view2.visibility = View.GONE
                    ratingstatics.close.visibility = View.INVISIBLE
                    setRatingDistributionUI(ratingDistribution, reviewCount, itemBinding)
                }
            }
        }
    }

    private fun setRatingDistributionUI(ratingDistribution: List<RatingDistribution>,
                                        reviewCount: Int,
                                        itemBinding: HeaderMoreReviewRecyclerViewBinding) {
        itemBinding.apply {
            for (rating in ratingDistribution) {
                when (rating.ratingValue) {
                    1 -> {
                        ratingstatics.progressbar1.progress =
                                Utils.calculatePercentage(rating.count, reviewCount)
                        ratingstatics.tv1StarRatingCount.text = rating.count.toString()
                    }
                    2 -> {
                        ratingstatics.progressbar2.progress =
                                Utils.calculatePercentage(rating.count, reviewCount)
                        ratingstatics.tv2StarRatingCount.text = rating.count.toString()
                    }
                    3 -> {
                        ratingstatics.progressbar3.progress =
                                Utils.calculatePercentage(rating.count, reviewCount)
                        ratingstatics.tv3StarRatingCount.text = rating.count.toString()
                    }
                    4 -> {
                        ratingstatics.progressbar4.progress =
                                Utils.calculatePercentage(rating.count, reviewCount)
                        ratingstatics.tv4StarRatingCount.text = rating.count.toString()

                    }
                    5 -> {
                        ratingstatics.progressbar5.progress =
                                Utils.calculatePercentage(rating.count, reviewCount)
                        ratingstatics.tv5StarRatingCount.text = rating.count.toString()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            MoreReviewHeaderAdapter.HeaderViewHolder {
        return HeaderViewHolder(
            HeaderMoreReviewRecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bindView()
    }

    override fun getItemViewType(position: Int): Int {
        return  R.layout.header_more_review_recycler_view
    }
}
