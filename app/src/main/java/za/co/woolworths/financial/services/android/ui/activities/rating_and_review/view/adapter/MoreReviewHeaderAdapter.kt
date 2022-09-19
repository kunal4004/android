package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.header_more_review_recycler_view.view.*
import kotlinx.android.synthetic.main.pdp_rating_layout.view.*
import kotlinx.android.synthetic.main.ratings_ratingdetails.view.*
import kotlinx.android.synthetic.main.review_count_layout.view.*
import kotlinx.android.synthetic.main.sort_and_refine_selection_layout.view.*
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

    inner class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bindView() {
            itemView.apply {
                close_top.visibility = View.GONE
                rating_details.text = context.getString(R.string.customer_reviews)
                pdpratings.visibility = View.VISIBLE
                sort_and_refine.visibility = View.VISIBLE
                refineProducts.setOnClickListener({ sortAndRefineListener.openRefineDrawer() })
                sortProducts.setOnClickListener( { sortAndRefineListener.openSortDrawer() })


                if (reviewStatistics.isEmpty()) {
                    return
                }
                reviewStatistics[0].apply {
                    val recommend= recommendedPercentage.split("%")
                    if (recommend.size == 2) {
                        tvRecommendPercent.text = "${recommend[0]}% "
                        tvRecommendTxtValue.text = recommend[1]
                    }
                    pdpratings.apply {
                        ratingBarTop.visibility = View.VISIBLE
                        tvTotalReviews.visibility = View.VISIBLE
                        ratingBarTop.rating = averageRating
                        tvTotalReviews.text = resources.getQuantityString(R.plurals.no_review, reviewCount, reviewCount)
                        tvTotalReviews.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    }
                    view_2.visibility = View.GONE
                    close.visibility = View.INVISIBLE
                    setRatingDistributionUI(ratingDistribution, reviewCount, itemView)
                }
            }
        }
    }

    private fun setRatingDistributionUI(ratingDistribution: List<RatingDistribution>,
                                        reviewCount: Int,
                                        itemView: View) {
        itemView.apply {
            for (rating in ratingDistribution) {
                when (rating.ratingValue) {
                    1 -> {
                        progressbar_1.progress =
                                Utils.calculatePercentage(rating.count, reviewCount)
                        tv_1_starRating_count.text = rating.count.toString()
                    }
                    2 -> {
                        progressbar_2.progress =
                                Utils.calculatePercentage(rating.count, reviewCount)
                        tv_2_starRating_count.text = rating.count.toString()
                    }
                    3 -> {
                        progressbar_3.progress =
                                Utils.calculatePercentage(rating.count, reviewCount)
                        tv_3_starRating_count.text = rating.count.toString()
                    }
                    4 -> {
                        progressbar_4.progress =
                                Utils.calculatePercentage(rating.count, reviewCount)
                        tv_4_starRating_count.text = rating.count.toString()

                    }
                    5 -> {
                        progressbar_5.progress =
                                Utils.calculatePercentage(rating.count, reviewCount)
                        tv_5_starRating_count.text = rating.count.toString()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            MoreReviewHeaderAdapter.HeaderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.header_more_review_recycler_view, parent, false)
        return HeaderViewHolder(itemView)
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
