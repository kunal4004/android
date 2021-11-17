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
import com.facebook.FacebookSdk
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.AdditionalFields
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.SecondaryRatings
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Thumbnails
import za.co.woolworths.financial.services.android.ui.adapters.ReviewThumbnailAdapter
import za.co.woolworths.financial.services.android.ui.adapters.SecondaryRatingAdapter

class RatingAndReviewUtils {

    companion object {

        fun setSecondaryRatingsUI(secondaryRatings: List<SecondaryRatings>, rvSecondaryRatings: RecyclerView) {
            rvSecondaryRatings.layoutManager = GridLayoutManager(FacebookSdk.getApplicationContext(), 2)
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
                    tvAdditionalFieldLabel.setTextAppearance(FacebookSdk.getApplicationContext(), R.style.myriad_pro_regular_black_15_text_style);
                    tvAdditionalFieldValue.setTextAppearance(FacebookSdk.getApplicationContext(), R.style.myriad_pro_semi_bold_black_15_text_style);
                } else {
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

        fun setReviewThumbnailUI(thumbnails: List<Thumbnails>,
                                 rvThumbnail: RecyclerView,
                                reviewThumbnailAdapter:ReviewThumbnailAdapter) {

            rvThumbnail.layoutManager = GridLayoutManager(FacebookSdk.getApplicationContext(), 3)
            rvThumbnail.adapter = reviewThumbnailAdapter
            if (thumbnails.size > 2) {
                reviewThumbnailAdapter.setDataList(thumbnails.subList(0, 2))
            } else
                reviewThumbnailAdapter.setDataList(thumbnails)
        }


    }
}