package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.content.Context
import android.graphics.Paint
import android.hardware.camera2.TotalCaptureResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.header_more_review_recycler_view.view.*
import kotlinx.android.synthetic.main.pdp_rating_layout.view.*
import kotlinx.android.synthetic.main.ratings_ratingdetails.view.*
import kotlinx.android.synthetic.main.review_count_layout.view.*
import kotlinx.android.synthetic.main.review_helpful_and_report_layout.view.*
import kotlinx.android.synthetic.main.review_row_layout.view.*
import kotlinx.android.synthetic.main.sort_and_refine_selection_layout.view.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingDistribution
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.ReviewStatistics
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Thumbnails
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.ui.adapters.ReviewThumbnailAdapter
import za.co.woolworths.financial.services.android.util.Utils

class MoreReviewsAdapter(val context: Context,
                         val reviewItemClickListener: ReviewItemClickListener,
                         val reportReviewOptions: List<String>?,
                         var mTotalPages: Int  ) : PagingDataAdapter<Reviews,
        RecyclerView.ViewHolder>(MoreReviewsComparator),
        ReviewThumbnailAdapter.ThumbnailClickListener {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    private lateinit var reviewThumbnailAdapter: ReviewThumbnailAdapter
    private var thumbnailFullList = listOf<Thumbnails>()

    interface ReviewItemClickListener {
        fun openSkinProfileDialog(reviews: Reviews)
        fun openReportScreen(reportReviewOptions: List<String>?)
        fun openReviewDetailsScreen(reviews: Reviews, reportReviewOptions: List<String>?)
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
                        tvReport.setOnClickListener {
                            reviewItemClickListener.openReportScreen(reportReviewOptions)
                        }
                    }

                    if (contextDataValue.isEmpty() && tagDimensions.isEmpty()) {
                        tvSkinProfile.visibility = View.GONE
                    }
                    tvSkinProfile.setOnClickListener {
                        reviewItemClickListener.openSkinProfileDialog(review)
                    }

                    linear_layout_customer_review.setOnClickListener {
                        reviewItemClickListener.openReviewDetailsScreen(review, reportReviewOptions)
                    }
                }
            }
        }
    }

    inner class ReviewHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView() {
            itemView.apply {
                tv_review_count.text = mTotalPages.toString()
            }
        }
    }

    fun setTotalPages(totalPages: Int) {
        this.mTotalPages = totalPages
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
                        .inflate(R.layout.review_count_layout, parent, false))
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