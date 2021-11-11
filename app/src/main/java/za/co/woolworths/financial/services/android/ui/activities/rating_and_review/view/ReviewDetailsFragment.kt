package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.facebook.FacebookSdk
import kotlinx.android.synthetic.main.product_quality_layout.view.*
import kotlinx.android.synthetic.main.review_detail_layout.*
import kotlinx.android.synthetic.main.review_detail_layout.rvSecondaryRatings
import kotlinx.android.synthetic.main.review_helpful_and_report_layout.*
import kotlinx.android.synthetic.main.review_row_layout.*
import kotlinx.android.synthetic.main.skin_profile_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.AdditionalFields
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Normal
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.SecondaryRatings
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ProductReviewViewPagerAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.SkinProfileAdapter
import za.co.woolworths.financial.services.android.ui.adapters.SecondaryRatingAdapter
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class ReviewDetailsFragment : Fragment() {

    private lateinit var productViewPagerAdapter: ProductReviewViewPagerAdapter
    private lateinit var secondaryRatingAdapter: SecondaryRatingAdapter


    companion object {
        fun newInstance() = ReviewDetailsFragment()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.review_detail_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.apply {
            val reviewData = Utils.jsonStringToObject(getString(KotlinUtils.REVIEW_DATA), Reviews::class.java) as Reviews
            setDefaultUi(reviewData)
            setProductImageViewPager(reviewData.photos.normal)
        }
    }

    private fun setDefaultUi(reviewData: Reviews?) {

        reviewData?.run {

            txt_date.text = submissionTime
            txt_reviewer_name.text = userNickname
            rating_bar.rating = rating
            tv_skin_label.text = title
            skin_detail.text = reviewText
            tvReport.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)

            setVerifiedBuyers(isVerifiedBuyer)
            setSkinProfielLayout(contextDataValue , tagDimensions)
            setReviewAdditionalFields(additionalFields)
            setSecondaryRatingsUI(secondaryRatings)
        }
    }

    private fun setReviewAdditionalFields(additionalFields: List<AdditionalFields>){
        for (additionalField in additionalFields){
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
            lladdiionField.addView(rootView)
        }
    }

    private fun setSecondaryRatingsUI(secondaryRatings: List<SecondaryRatings>){
        rvSecondaryRatings.layoutManager = GridLayoutManager(FacebookSdk.getApplicationContext(),2)
        secondaryRatingAdapter = SecondaryRatingAdapter()
        rvSecondaryRatings.adapter = secondaryRatingAdapter
        secondaryRatingAdapter.setDataList(secondaryRatings)
    }

    private fun setSkinProfielLayout(contextDataValue: List<SkinProfile>, tagDimensions: List<SkinProfile>) {
        if (contextDataValue.isNotEmpty() || tagDimensions.isNotEmpty()) {
            skin_profile_layout.rv_skin_profile.visibility = View.VISIBLE
        } else {
            skin_profile_layout.rv_skin_profile.visibility = View.GONE
        }

        skin_profile_layout.rv_skin_profile.layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false)
        val list: List<SkinProfile> = contextDataValue.plus(tagDimensions)
        skin_profile_layout.rv_skin_profile.adapter = SkinProfileAdapter(list)
        skin_profile_layout.rv_skin_profile.addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL))
    }

    private fun setVerifiedBuyers(isVerifiedBuyer: Boolean) {
        if (isVerifiedBuyer) {
            txt_verified.visibility = View.VISIBLE
        } else {
            txt_verified.visibility = View.GONE
        }
    }

    private fun setProductImageViewPager(photos: List<Normal>) {
        if (photos.isEmpty()) {
            reviewProductImagesViewPager.visibility = View.GONE
            return
        }
        activity?.apply {
            productViewPagerAdapter = ProductReviewViewPagerAdapter(context, photos)
                    .apply {
                        reviewProductImagesViewPager.let { pager ->
                            pager.adapter = this
                            tabDots.setupWithViewPager(pager, true)
                        }
                    }
        }
    }
}
