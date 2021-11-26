package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pdp_rating_layout.view.*
import kotlinx.android.synthetic.main.ratings_ratingdetails.view.*
import kotlinx.android.synthetic.main.review_helpful_and_report_layout.view.*
import kotlinx.android.synthetic.main.review_row_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingDistribution
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.ReviewStatistics
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Thumbnails
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.ui.adapters.ReviewThumbnailAdapter
import za.co.woolworths.financial.services.android.util.Utils

class MoreReviewsAdapter(val context: Context,
                         val skinProfileDialogListener: SkinProfileDialogOpenListener,
                         val reviewStatistics: ReviewStatistics) : PagingDataAdapter<Reviews,
        RecyclerView.ViewHolder>(MoreReviewsComparator), ReviewThumbnailAdapter.ThumbnailClickListener {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    private lateinit var reviewThumbnailAdapter: ReviewThumbnailAdapter
    private var thumbnailFullList = listOf<Thumbnails>()


    interface SkinProfileDialogOpenListener {
        fun openSkinProfileDialog(reviews: Reviews)
    }

    inner class ReviewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(review: Reviews?) {
            itemView.apply {
                review?.apply {
                    tvName.text = userNickname
                    if (isVerifiedBuyer)
                        tvVerifiedBuyer.visibility = View.VISIBLE
                    else
                        tvVerifiedBuyer.visibility = View.GONE
                    if (isStaffMember)
                        tvVerifiedStaffMember.visibility = View.VISIBLE
                    else
                        tvVerifiedStaffMember.visibility = View.GONE
                    ratingBar.rating = rating
                    tvReviewHeading.text = title
                    tvCustomerReview.text = reviewText
                    tvReviewPostedOn.text = syndicatedSource
                    tvDate.text = submissionTime
                    RatingAndReviewUtil.setReviewAdditionalFields(additionalFields, llAdditionalFields, context)
                    RatingAndReviewUtil.setSecondaryRatingsUI(secondaryRatings, rvSecondaryRatings, context)
                    setReviewThumbnailUI(photos.thumbnails, rvThumbnail)
                    thumbnailFullList = photos.thumbnails
                    tvSkinProfile.paintFlags = Paint.UNDERLINE_TEXT_FLAG

                    reviewHelpfulReport.apply {
                        tvReport.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    }

                    if (contextDataValue.isEmpty() && tagDimensions.isEmpty()) {
                        tvSkinProfile.visibility = View.GONE
                    }
                    tvSkinProfile.setOnClickListener {
                        skinProfileDialogListener.openSkinProfileDialog(review)
                    }
                }
            }
        }
    }

    inner class ReviewHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView() {
            itemView.apply {
                close_top.visibility = View.GONE
                rating_details.text = context.getString(R.string.customer_reviews)
                pdpratings.visibility = View.VISIBLE

                reviewStatistics.apply {
                    recommend.text = recommendedPercentage
                    pdpratings.apply {
                        ratingBarTop.visibility = View.VISIBLE
                        tvTotalReviews.visibility = View.VISIBLE
                        ratingBarTop.rating = averageRating
                        tvTotalReviews.text = context.getString(R.string.customer_reviews)
                    }
                    recommend.text = recommendedPercentage
                    view_2.visibility = View.GONE
                    close.visibility = View.INVISIBLE
                    setRatingDistributionUI(ratingDistribution, reviewCount, itemView)
                }
            }
        }
    }

    fun setReviewThumbnailUI(thumbnails: List<Thumbnails>,
                             rvThumbnail: RecyclerView) {
        reviewThumbnailAdapter = ReviewThumbnailAdapter(context, this)
        RatingAndReviewUtil.setReviewThumbnailUI(thumbnails, rvThumbnail, reviewThumbnailAdapter, context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ReviewsViewHolder) {
            holder.bindView(getItem(position))
        } else if (holder is ReviewHeaderViewHolder) {
            holder.bindView()
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
            RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) {
            return ReviewsViewHolder(
                    LayoutInflater
                            .from(parent.context)
                            .inflate(R.layout.review_row_layout, parent, false)
            )
        }
        return ReviewHeaderViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.ratings_ratingdetails, parent, false))


    }

    object MoreReviewsComparator : DiffUtil.ItemCallback<Reviews>() {

        override fun areItemsTheSame(oldReview: Reviews, newReview: Reviews): Boolean {
            return oldReview.id == newReview.id
        }

        override fun areContentsTheSame(oldReview: Reviews, newReview: Reviews): Boolean {
            return oldReview == newReview
        }
    }

    override fun thumbnailClicked() {
        reviewThumbnailAdapter.setDataList(thumbnailFullList)
        reviewThumbnailAdapter.notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private fun isPositionHeader(position: Int): Boolean {
        return position == 0
    }

}