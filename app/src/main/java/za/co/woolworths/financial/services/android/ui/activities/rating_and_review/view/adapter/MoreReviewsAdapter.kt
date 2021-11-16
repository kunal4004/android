package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.facebook.FacebookSdk
import kotlinx.android.synthetic.main.review_row_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.AdditionalFields
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews

class MoreReviewsAdapter(val context: Context) : PagingDataAdapter<Reviews,
        MoreReviewsAdapter.ReviewsViewHolder>(MoreReviewsComparator) {

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
                    setReviewAdditionalFields(additionalFields, llAdditionalFields)
//                    setSecondaryRatingsUI(secondaryRatings)
//                    setReviewThumbnailUI(photos.thumbnails, rvThumbnail)
                    if(contextDataValue.isEmpty()){
                        tvSkinProfile.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setReviewAdditionalFields(additionalFields: List<AdditionalFields>,
                                          llAdditionalFields: LinearLayout){

        for (additionalField in additionalFields) {
            val rootView = LinearLayout(context)
            rootView.layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            rootView.orientation = LinearLayout.HORIZONTAL

            val tvAdditionalFieldLabel = TextView(context)
            tvAdditionalFieldLabel.alpha = 0.5F
            val tvAdditionalFieldValue = TextView(context)
            tvAdditionalFieldValue.alpha = 0.5F
            val ivCircle = ImageView(context)
            val tvParam: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            tvParam.setMargins(25, 0, 0, 8)
            tvAdditionalFieldValue.layoutParams = tvParam
            val ivParam: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            ivParam.setMargins(25,15,0,0)
            ivCircle.layoutParams = ivParam

            if (Build.VERSION.SDK_INT < 23) {
                tvAdditionalFieldLabel.setTextAppearance(FacebookSdk.getApplicationContext(), R.style.myriad_pro_regular_black_15_text_style);
                tvAdditionalFieldValue.setTextAppearance(FacebookSdk.getApplicationContext(), R.style.myriad_pro_semi_bold_black_15_text_style);
            } else{
                tvAdditionalFieldLabel.setTextAppearance(R.style.myriad_pro_regular_black_15_text_style);
                tvAdditionalFieldValue.setTextAppearance(R.style.myriad_pro_semi_bold_black_15_text_style);
            }
            tvAdditionalFieldLabel.text = additionalField.label
            ivCircle.setImageResource(R.drawable.ic_circle)
            tvAdditionalFieldValue.text = additionalField.valueLabel

            rootView.addView(tvAdditionalFieldLabel)
            rootView.addView(ivCircle)
            rootView.addView(tvAdditionalFieldValue)
            llAdditionalFields.addView(rootView)
        }
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
}