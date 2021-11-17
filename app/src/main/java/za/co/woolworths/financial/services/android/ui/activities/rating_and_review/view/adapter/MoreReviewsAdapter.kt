package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.facebook.FacebookSdk
import kotlinx.android.synthetic.main.review_row_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Thumbnails
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtils
import za.co.woolworths.financial.services.android.ui.adapters.ReviewThumbnailAdapter

class MoreReviewsAdapter(val context: Context, val skinProfileDialogListener: SkinProfileDialogOpenListener) : PagingDataAdapter<Reviews,
        MoreReviewsAdapter.ReviewsViewHolder>(MoreReviewsComparator) , ReviewThumbnailAdapter.ThumbnailClickListener {

    private lateinit var reviewThumbnailAdapter: ReviewThumbnailAdapter
    private var thumbnailFullList = listOf<Thumbnails>()

    interface SkinProfileDialogOpenListener {
        fun openSkinProfileDialog(reviews: Reviews)
    }

    inner class ReviewsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)  {

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
                    RatingAndReviewUtils.setReviewAdditionalFields(additionalFields, llAdditionalFields, context)
                    RatingAndReviewUtils.setSecondaryRatingsUI(secondaryRatings, rvSecondaryRatings)
                    setReviewThumbnailUI(photos.thumbnails, rvThumbnail)
                    thumbnailFullList = photos.thumbnails
                    if(contextDataValue.isEmpty() && tagDimensions.isEmpty()){
                        tvSkinProfile.visibility = View.GONE
                    }
                    tvSkinProfile.setOnClickListener {
                        skinProfileDialogListener.openSkinProfileDialog(review)
                    }
                }
            }
        }
    }

    fun setReviewThumbnailUI(thumbnails: List<Thumbnails>,
                             rvThumbnail: RecyclerView) {
        reviewThumbnailAdapter = ReviewThumbnailAdapter(context, this)
        RatingAndReviewUtils.setReviewThumbnailUI(thumbnails, rvThumbnail, reviewThumbnailAdapter)
    }

    override fun onBindViewHolder(holder: MoreReviewsAdapter.ReviewsViewHolder, position: Int) {
        holder.bindView(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            MoreReviewsAdapter.ReviewsViewHolder {
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
}