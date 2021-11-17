package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.review_detail_layout.*
import kotlinx.android.synthetic.main.review_detail_layout.rvSecondaryRatings
import kotlinx.android.synthetic.main.review_helpful_and_report_layout.*
import kotlinx.android.synthetic.main.review_row_layout.*
import kotlinx.android.synthetic.main.skin_profile_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Normal
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtils
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ProductReviewViewPagerAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.SkinProfileAdapter
import za.co.woolworths.financial.services.android.ui.adapters.SecondaryRatingAdapter
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class ReviewDetailsFragment : Fragment(){

    private lateinit var productViewPagerAdapter: ProductReviewViewPagerAdapter

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
            RatingAndReviewUtils.setReviewAdditionalFields(additionalFields, lladdiionField, requireContext())
            RatingAndReviewUtils.setSecondaryRatingsUI(secondaryRatings, rvSecondaryRatings, requireContext())
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
