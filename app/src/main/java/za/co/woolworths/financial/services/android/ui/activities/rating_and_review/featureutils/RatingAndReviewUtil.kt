package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils

import android.content.Context
import android.os.Build
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.AdditionalFields
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.SecondaryRatings
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Thumbnails
import za.co.woolworths.financial.services.android.ui.adapters.ReviewThumbnailAdapter
import za.co.woolworths.financial.services.android.ui.adapters.SecondaryRatingAdapter
import za.co.woolworths.financial.services.android.util.Utils

class RatingAndReviewUtil {

    companion object {

        var isComingFromMoreReview:Boolean = false

        var isSuccessFullyReported:Boolean = false
        var likedReviews: MutableList<String> = mutableListOf()
        var reportedReviews: MutableList<String> = mutableListOf()


        fun isRatingAndReviewConfigavailbel () =AppConfigSingleton.ratingsAndReviews?.isEnabled ?: false


        fun setSecondaryRatingsUI(secondaryRatings: List<SecondaryRatings>,
                                  rvSecondaryRatings: RecyclerView, context: Context) {
            rvSecondaryRatings.layoutManager = GridLayoutManager(context, 2)
            val secondaryRatingAdapter = SecondaryRatingAdapter()
            rvSecondaryRatings.adapter = secondaryRatingAdapter
            secondaryRatingAdapter.setDataList(secondaryRatings)
        }

        fun setReviewAdditionalFields(additionalFields: List<AdditionalFields>,
                                      llAdditionalFields: LinearLayout,
                                      context: Context) {

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
                ivParam.setMargins(25, 15, 0, 0)
                ivCircle.layoutParams = ivParam

                if (Build.VERSION.SDK_INT < 23) {
                    tvAdditionalFieldLabel.setTextAppearance(context, R.style.opensans_regular_13_black);
                    tvAdditionalFieldValue.setTextAppearance(context, R.style.opensans_semi_bold_13_text_style);
                } else {
                    tvAdditionalFieldLabel.setTextAppearance(R.style.opensans_regular_13_black);
                    tvAdditionalFieldValue.setTextAppearance(R.style.opensans_semi_bold_13_text_style);
                }
                tvAdditionalFieldLabel.text = additionalField.label
                ivCircle.setImageResource(R.drawable.ic_circle)
                tvAdditionalFieldValue.text = additionalField.valueLabel

                rootView.addView(tvAdditionalFieldLabel)
                rootView.addView(ivCircle)
                rootView.addView(tvAdditionalFieldValue)
                llAdditionalFields.removeAllViews()
                llAdditionalFields.addView(rootView)
            }
        }

        fun setReviewThumbnailUI(thumbnails: List<Thumbnails>,
                                 rvThumbnail: RecyclerView,
                                 reviewThumbnailAdapter: ReviewThumbnailAdapter,
                                 context: Context) {

            rvThumbnail.layoutManager = GridLayoutManager(context, 3)
            rvThumbnail.adapter = reviewThumbnailAdapter
            if (thumbnails.size > 2) {
                reviewThumbnailAdapter.setDataList(thumbnails.subList(0, 2))
            } else
                reviewThumbnailAdapter.setDataList(thumbnails)
        }
    }
}