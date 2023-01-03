package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ReviewDetailLayoutBinding
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
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ReviewDetailsFragment : BaseFragmentBinding<ReviewDetailLayoutBinding>(ReviewDetailLayoutBinding::inflate) {

    private lateinit var productViewPagerAdapter: ProductReviewViewPagerAdapter
    private lateinit var moreReviewViewModel: RatingAndReviewViewModel
    var reportReviewFragment: ReportReviewFragment? = null
    private lateinit var reviews: Reviews
    private lateinit var reportReviewOptions: List<String>
    private var apiCallInProgress = false

    companion object {
        fun newInstance() = ReviewDetailsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.apply {
            btnBack?.setOnClickListener {
                activity?.onBackPressed()
            }
            txtToolbarTitle.text = getString(R.string.review_details)
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
        binding.progressBar.visibility = View.VISIBLE
        apiCallInProgress = true
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        apiCallInProgress = false
    }

    private fun setDefaultUi(reviewData: Reviews?, reportReviewOptions: List<String>) {
        binding.apply {
            reviewData?.run {
                txtDate.text = submissionTime
                txtReviewerName.text = userNickname
                ratingBar.rating = rating
                tvSkinLabel.text = title
                skinDetail.text = reviewText
                binding.layoutHelpfulReview.tvLikes.text = totalPositiveFeedbackCount.toString()

                if (RatingAndReviewUtil.reportedReviews.contains(id.toString())) {
                    binding.layoutHelpfulReview.tvReport.setTextColor(Color.RED)
                    binding.layoutHelpfulReview.tvReport.text = getString(R.string.reported)
                    binding.layoutHelpfulReview.tvReport?.setTypeface(binding.layoutHelpfulReview.tvReport.typeface, Typeface.BOLD)
                    RatingAndReviewUtil.isSuccessFullyReported = false
                }

                binding.layoutHelpfulReview.tvReport.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)

                if (RatingAndReviewUtil.likedReviews.contains(id.toString())) {
                    binding.layoutHelpfulReview.ivLike.setImageResource(R.drawable.iv_like_selected)
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
                layoutHelpfulReview.apply {
                    tvReport.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    tvReport.setOnClickListener {
                        if (!SessionUtilities.getInstance().isUserAuthenticated) {
                            ScreenManager.presentSSOSignin(activity)
                        } else {
                            openReportScreen(reviewData, reportReviewOptions)
                        }
                    }
                    binding.layoutHelpfulReview.ivLike.setOnClickListener {
                        likeButtonClicked()
                    }
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
                        binding.layoutHelpfulReview.ivLike.setImageResource(R.drawable.iv_like_selected)
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
        binding.apply {
            if (contextDataValue.isNotEmpty() || tagDimensions.isNotEmpty()) {
                skinProfileLayout.rvSkinProfile.visibility = View.VISIBLE
            } else {
                skinProfileLayout.rvSkinProfile.visibility = View.GONE
            }

            skinProfileLayout.rvSkinProfile.layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
            val list: List<SkinProfile> = contextDataValue.plus(tagDimensions)
            skinProfileLayout.rvSkinProfile.adapter = SkinProfileAdapter(list)
            skinProfileLayout.rvSkinProfile.addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL)
            )
        }
    }

    private fun setVerifiedBuyers(isVerifiedBuyer: Boolean) {
        binding.apply {
            if (isVerifiedBuyer) {
                txtVerified.visibility = View.VISIBLE
            } else {
                txtVerified.visibility = View.GONE
            }
        }
    }

    private fun setProductImageViewPager(photos: List<Normal>) {
        binding.apply {
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
}
