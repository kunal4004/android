package za.co.woolworths.financial.services.android.ui.activities.rating_and_review

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.product_quality_layout.view.*
import kotlinx.android.synthetic.main.review_detail_layout.*
import kotlinx.android.synthetic.main.review_helpful_and_report_layout.*
import kotlinx.android.synthetic.main.skin_profile_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Normal
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.SecondaryRatings
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class ReviewDetailsFragment : Fragment() {

    private lateinit var productViewPagerAdapter: ProductReviewViewPagerAdapter

    companion object {
        fun newInstance() = ReviewDetailsFragment()
        val SLIDER = "SLIDER"
        val NORMAL = "NORMAL"
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
            setSecondaryRatingLayout(secondaryRatings)
        }
    }

    private fun setSecondaryRatingLayout(secondaryRatings: List<SecondaryRatings>) {
        if (secondaryRatings.isEmpty()) {
            view_product_quality.visibility = View.GONE
        } else {
            view_product_quality.visibility = View.VISIBLE

            for (secondaryRating in secondaryRatings) {
                if (secondaryRating.displayType.equals(SLIDER)) {
                    view_product_quality.txtMinLabel.text = secondaryRating.minLabel
                    view_product_quality.txtMaxLabel.text = secondaryRating.maxLabel
                }
                if (secondaryRating.displayType.equals(NORMAL)) {
                    view_product_quality.progress_fit_quality.visibility = View.GONE
                    view_product_quality.txtMinLabel.visibility = View.GONE
                    view_product_quality.txtMaxLabel.visibility = View.GONE
                    view_product_quality.txt_fit_label.visibility = View.GONE
                    view_product_quality.txt_product_quality_label.text = secondaryRating.label
                    view_product_quality.txt_product_quality_value.text = secondaryRating.value.toString().plus(getString(R.string.slash)).plus(secondaryRating.valueRange
                    )
                }
            }
        }
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
