package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ReviewRowLayoutBinding
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Thumbnails
import za.co.woolworths.financial.services.android.ui.adapters.ReviewThumbnailAdapter

class MoreReviewsAdapter(
    var context: Context,
    val reviewItemClickListener: ReviewItemClickListener,
    var reportReviewOptions: List<String>?,
    var reportPosiionList: MutableList<Int>
) : PagingDataAdapter<Reviews,
        MoreReviewsAdapter.ReviewsViewHolder>(MoreReviewsComparator),
    ReviewThumbnailAdapter.ThumbnailClickListener {

    private lateinit var reviewThumbnailAdapter: ReviewThumbnailAdapter
    private var thumbnailFullList = listOf<Thumbnails>()
    private var likeClickedPosition = -1
    interface ReviewItemClickListener {
        fun openSkinProfileDialog(reviews: Reviews)
        fun openReportScreen(reviews: Reviews, reportReviewOptions: List<String>?)
        fun openReviewDetailsScreen(reviews: Reviews, reportReviewOptions: List<String>?)
        fun reviewHelpfulClicked(review: Reviews)
    }

    fun setReviewOptionsList(reportReviewOptions: List<String>?) {
        this.reportReviewOptions = reportReviewOptions
    }

    inner class ReviewsViewHolder(val itemBinding: ReviewRowLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bindView(review: Reviews?, position: Int) {
            itemBinding.apply {
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
                    reviewHelpfulReport.tvLikes.text = totalPositiveFeedbackCount.toString()
                    if (RatingAndReviewUtil.likedReviews.contains(review.id.toString()))
                        reviewHelpfulReport.ivLike.setImageResource(R.drawable.iv_like_selected)
                    else
                        reviewHelpfulReport.ivLike.setImageResource(R.drawable.iv_like)
                    RatingAndReviewUtil.setReviewAdditionalFields(
                        additionalFields,
                        llAdditionalFields,
                        context
                    )
                    RatingAndReviewUtil.setSecondaryRatingsUI(
                        secondaryRatings,
                        rvSecondaryRatings,
                        context
                    )
                    setReviewThumbnailUI(photos.thumbnails, rvThumbnail)
                    thumbnailFullList = photos.thumbnails
                    tvSkinProfile.paintFlags = Paint.UNDERLINE_TEXT_FLAG

                    reviewHelpfulReport.apply {
                        if(RatingAndReviewUtil.reportedReviews.contains(review.id.toString())){
                            tvReport.setTextColor(Color.RED)
                            tvReport.setText(root.resources.getString(R.string.reported))
                            tvReport?.setTypeface(tvReport.typeface, Typeface.BOLD)
                            RatingAndReviewUtil.isSuccessFullyReported = false
                        }else{
                            tvReport.setTextColor(Color.BLACK)
                            tvReport.setText(root.resources.getString(R.string.report))
                        }
                        tvReport.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                        tvReport.setOnClickListener {
                            reportPosiionList.add(position)
                            reviewItemClickListener.openReportScreen(review, reportReviewOptions)
                        }
                    }

                    if (contextDataValue.isEmpty() && tagDimensions.isEmpty()) {
                        tvSkinProfile.visibility = View.GONE
                    }
                    tvSkinProfile.setOnClickListener {
                        reviewItemClickListener.openSkinProfileDialog(review)
                    }

                    linearLayoutCustomerReview.setOnClickListener {
                        reviewItemClickListener.openReviewDetailsScreen(review, reportReviewOptions)
                    }

                    reviewHelpfulReport.ivLike.setOnClickListener {
                        reviewItemClickListener.reviewHelpfulClicked(review)
                    }
                }
            }
        }
    }

    fun setReviewThumbnailUI(
        thumbnails: List<Thumbnails>,
        rvThumbnail: RecyclerView
    ) {
        reviewThumbnailAdapter = ReviewThumbnailAdapter(context, this)
        RatingAndReviewUtil.setReviewThumbnailUI(
            thumbnails,
            rvThumbnail,
            reviewThumbnailAdapter,
            context
        )
    }

    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        holder.bindView(getItem(position), position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        return ReviewsViewHolder(
            ReviewRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
