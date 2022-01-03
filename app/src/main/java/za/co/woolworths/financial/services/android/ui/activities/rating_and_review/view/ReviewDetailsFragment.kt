package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.common_toolbar.*
import kotlinx.android.synthetic.main.review_detail_layout.*
import kotlinx.android.synthetic.main.review_detail_layout.rvSecondaryRatings
import kotlinx.android.synthetic.main.review_helpful_and_report_layout.*
import kotlinx.android.synthetic.main.review_helpful_and_report_layout.view.*
import kotlinx.android.synthetic.main.skin_profile_layout.view.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Normal
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingReviewResponse
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.SkinProfile
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ProductReviewViewPagerAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.SkinProfileAdapter
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import java.util.ArrayList

class ReviewDetailsFragment : Fragment() {

    private lateinit var productViewPagerAdapter: ProductReviewViewPagerAdapter

    var reportReviewFragment: ReportReviewFragment? = null

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
        toolbar?.let {
            btn_back?.setOnClickListener {
                activity?.onBackPressed()
            }
            txt_toolbar_title.text = getString(R.string.review_details)
        }

        arguments?.apply {
            if (RatingAndReviewUtil.isComingFromMoreReview) {
                RatingAndReviewUtil.isComingFromMoreReview = false
                val reviews = getSerializable(KotlinUtils.REVIEW_DATA) as Reviews
                val reportReviewOptions = getStringArrayList(KotlinUtils.REVIEW_REPORT)
                if (reviews != null && reportReviewOptions != null) {
                    setDefaultUi(reviews, reportReviewOptions)
                    setProductImageViewPager(reviews.photos.normal)
                }
            } else {
                val ratingAndResponseData = Utils.jsonStringToObject(
                        getString(KotlinUtils.REVIEW_DATA),
                        RatingReviewResponse::class.java
                ) as RatingReviewResponse
                val reviews = ratingAndResponseData.reviews.get(0)
                setDefaultUi(reviews, ratingAndResponseData.reportReviewOptions)
                setProductImageViewPager(reviews.photos.normal)
            }
        }
    }

    private fun setDefaultUi(reviewData: Reviews?, reportReviewOptions: List<String>) {

        reviewData?.run {
            txt_date.text = submissionTime
            txt_reviewer_name.text = userNickname
            rating_bar.rating = rating
            tv_skin_label.text = title
            skin_detail.text = reviewText
            if (RatingAndReviewUtil.isSuccessFullyReported) {
                tvReport.setTextColor(Color.RED)
                RatingAndReviewUtil.isSuccessFullyReported = false
            }

            tvReport.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)
            setVerifiedBuyers(isVerifiedBuyer)
            setSkinProfielLayout(contextDataValue, tagDimensions)
            RatingAndReviewUtil.setReviewAdditionalFields(
                additionalFields,
                lladdiionField,
                requireContext()
            )
            RatingAndReviewUtil.setSecondaryRatingsUI(
                secondaryRatings,
                rvSecondaryRatings,
                requireContext()
            )
            layout_helpful_review.apply {
                tvReport.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                tvReport.setOnClickListener {
                    openReportScreen(reportReviewOptions)
                }
            }
        }
    }

    private fun openReportScreen(reportReviewOptions: List<String>) {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSignin(activity)
        } else {
            val bundle = Bundle()
            bundle.putStringArrayList(
                    KotlinUtils.REVIEW_REPORT,
                    reportReviewOptions as ArrayList<String>
            )
            reportReviewFragment = ReportReviewFragment.newInstance()
            reportReviewFragment?.arguments = bundle
            activity?.apply {
                reportReviewFragment?.let {
                    val fragmentManager = supportFragmentManager
                    val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.content_main_frame, reportReviewFragment!!)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                }
            }
        }
    }

    private fun setSkinProfielLayout(
            contextDataValue: List<SkinProfile>,
            tagDimensions: List<SkinProfile>
    ) {
        if (contextDataValue.isNotEmpty() || tagDimensions.isNotEmpty()) {
            skin_profile_layout.rv_skin_profile.visibility = View.VISIBLE
        } else {
            skin_profile_layout.rv_skin_profile.visibility = View.GONE
        }

        skin_profile_layout.rv_skin_profile.layoutManager = LinearLayoutManager(
            activity, LinearLayoutManager.VERTICAL, false
        )
        val list: List<SkinProfile> = contextDataValue.plus(tagDimensions)
        skin_profile_layout.rv_skin_profile.adapter = SkinProfileAdapter(list)
        skin_profile_layout.rv_skin_profile.addItemDecoration(
            DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL)
        )
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
