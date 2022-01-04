package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R

import kotlinx.android.synthetic.main.review_helpful_and_report_layout.view.*
import kotlinx.android.synthetic.main.review_row_layout.view.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Thumbnails
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.ui.adapters.ReviewThumbnailAdapter

class MoreReviewsAdapter(var context: Context,
                         val reviewItemClickListener: ReviewItemClickListener,
                         var reportReviewOptions: List<String>?,
                         var reportPosiionList: MutableList<Int>) : PagingDataAdapter<Reviews,
        MoreReviewsAdapter.ReviewsViewHolder>(MoreReviewsComparator),
        ReviewThumbnailAdapter.ThumbnailClickListener {

    private lateinit var reviewThumbnailAdapter: ReviewThumbnailAdapter
    private var thumbnailFullList = listOf<Thumbnails>()

    interface ReviewItemClickListener {
        fun openSkinProfileDialog(reviews: Reviews)
        fun openReportScreen(reportReviewOptions: List<String>?)
        fun openReviewDetailsScreen(reviews: Reviews, reportReviewOptions: List<String>?)
    }

    fun setReviewOptionsList(reportReviewOptions: List<String>?) {
        this.reportReviewOptions = reportReviewOptions
    }

    inner class ReviewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(review: Reviews?, position: Int) {
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
                        if (reportPosiionList.contains(position) && RatingAndReviewUtil.isSuccessFullyReported) {
                            tvReport.setTextColor(Color.RED)
                            tvReport.setText(resources.getString(R.string.reported))
                            RatingAndReviewUtil.isSuccessFullyReported = false
                        }
                        tvReport.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                        tvReport.setOnClickListener {
                            reportPosiionList.add(position)
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

    fun setReviewThumbnailUI(thumbnails: List<Thumbnails>,
                             rvThumbnail: RecyclerView) {
        reviewThumbnailAdapter = ReviewThumbnailAdapter(context, this)
        RatingAndReviewUtil.setReviewThumbnailUI(thumbnails, rvThumbnail, reviewThumbnailAdapter, context)
    }

    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        holder.bindView(getItem(position), position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ReviewsViewHolder {
        return ReviewsViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.review_row_layout, parent, false)
        )
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
        return position
    }
}
