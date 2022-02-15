package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.common_toolbar.*
import kotlinx.android.synthetic.main.fragment_report_review.*
import kotlinx.android.synthetic.main.review_detail_layout.*
import kotlinx.android.synthetic.main.review_detail_layout.progressBar
import kotlinx.android.synthetic.main.review_detail_layout.rvSecondaryRatings
import kotlinx.android.synthetic.main.review_detail_layout.toolbar
import kotlinx.android.synthetic.main.review_helpful_and_report_layout.*
import kotlinx.android.synthetic.main.review_helpful_and_report_layout.view.*
import kotlinx.android.synthetic.main.skin_profile_layout.view.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper.RatingAndReviewApiHelper
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ProductReviewViewPagerAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.SkinProfileAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModel
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModelFactory
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import java.util.ArrayList

class ReviewDetailsFragment : Fragment() {

    private lateinit var productViewPagerAdapter: ProductReviewViewPagerAdapter
    private lateinit var moreReviewViewModel: RatingAndReviewViewModel
    var reportReviewFragment: ReportReviewFragment? = null
    private lateinit var reviews: Reviews
    private lateinit var reportReviewOptions: List<String>
    private var apiCallInProgress = false

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
                reviews = getSerializable(KotlinUtils.REVIEW_DATA) as Reviews
                reportReviewOptions = getStringArrayList(KotlinUtils.REVIEW_REPORT)!!
                if (reportReviewOptions != null) {
                    setDefaultUi(reviews, reportReviewOptions as ArrayList<String>)
                    setProductImageViewPager(reviews.photos.normal)
                }
            } else {
                if (getString(KotlinUtils.REVIEW_DATA) != null) {
                    val ratingAndResponseData = Utils.jsonStringToObject(
                        getString(KotlinUtils.REVIEW_DATA),
                        RatingReviewResponse::class.java
                    ) as RatingReviewResponse
                    reviews = ratingAndResponseData.reviews.get(0)
                    reportReviewOptions = ratingAndResponseData.reportReviewOptions
                }
                setDefaultUi(reviews, reportReviewOptions)
                setProductImageViewPager(reviews.photos.normal)
            }
        }
        setupViewModel()
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        apiCallInProgress = true
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
        apiCallInProgress = false
    }

    private fun setDefaultUi(reviewData: Reviews?, reportReviewOptions: List<String>) {

        reviewData?.run {
            txt_date.text = submissionTime
            txt_reviewer_name.text = userNickname
            rating_bar.rating = rating
            tv_skin_label.text = title
            skin_detail.text = reviewText
            tvLikes.text = totalPositiveFeedbackCount.toString()

            if (RatingAndReviewUtil.reportedReviews.contains(id.toString())) {
                tvReport.setTextColor(Color.RED)
                tvReport.text = getString(R.string.reported)
                tvReport?.setTypeface(tvReport.typeface, Typeface.BOLD)
                RatingAndReviewUtil.isSuccessFullyReported = false
            }

            tvReport.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)

            if (RatingAndReviewUtil.likedReviews.contains(id.toString())) {
                iv_like.setImageResource(R.drawable.iv_like_selected)
            }
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
                    if (!SessionUtilities.getInstance().isUserAuthenticated) {
                        ScreenManager.presentSSOSignin(activity)
                    } else {
                        openReportScreen(reviewData, reportReviewOptions)
                    }
                }
                iv_like.setOnClickListener {
                    likeButtonClicked()
                }
            }
        }
    }

    private fun openReportScreen(reviews: Reviews, reportReviewOptions: List<String>) {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSignin(activity)
        } else {
            val bundle = Bundle()
            bundle.putStringArrayList(
                KotlinUtils.REVIEW_REPORT,
                reportReviewOptions as ArrayList<String>
            )
            bundle.putSerializable(
                KotlinUtils.REVIEW_DATA,
                reviews
            )
            reportReviewFragment = ReportReviewFragment.newInstance()
            reportReviewFragment?.arguments = bundle
            activity?.apply {
                reportReviewFragment?.let {
                    val fragmentManager = supportFragmentManager
                    val fragmentTransaction: FragmentTransaction =
                        fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.content_main_frame, reportReviewFragment!!)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                }
            }
        }
    }

    private fun setupViewModel() {
        moreReviewViewModel = ViewModelProvider(
            this,
            RatingAndReviewViewModelFactory(RatingAndReviewApiHelper())
        ).get(RatingAndReviewViewModel::class.java)
    }

    private fun likeButtonClicked() {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSignin(activity)
        } else {
            lifecycleScope.launch {
                showProgressBar()
                try {
                    val response = moreReviewViewModel.reviewFeedback(
                        ReviewFeedback(
                            reviews.id.toString(),
                            SessionUtilities.getInstance().jwt.AtgId.asString,
                            KotlinUtils.REWIEW,
                            KotlinUtils.HELPFULNESS,
                            KotlinUtils.POSITIVE,
                            null
                        )
                    )
                    hideProgressBar()
                    if (response.httpCode == 200) {
                        RatingAndReviewUtil.likedReviews.add(reviews.id.toString())
                        iv_like.setImageResource(R.drawable.iv_like_selected)
                    }
                } catch (e: HttpException) {
                    e.printStackTrace()
                    hideProgressBar()
                    if (e.code() != 502) {
                        activity?.supportFragmentManager?.let { fragmentManager ->
                            Utils.showGeneralErrorDialog(
                                fragmentManager,
                                getString(R.string.statement_send_email_false_desc)
                            )
                        }
                    }
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
