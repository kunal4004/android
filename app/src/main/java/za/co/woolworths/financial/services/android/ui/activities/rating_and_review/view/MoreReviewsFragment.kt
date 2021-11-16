package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_more_reviews.*
import kotlinx.android.synthetic.main.pdp_rating_layout.*
import kotlinx.android.synthetic.main.ratings_ratingdetails.*
import kotlinx.android.synthetic.main.ratings_ratingdetails.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingDistribution
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingReviewResponse
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.ReviewStatistics
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.newtwork.apihelper.RatingAndReviewApiHelper
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.MoreReviewLoadStateAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.MoreReviewsAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModel
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModelFactory
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class MoreReviewsFragment : Fragment() {

    companion object {
        fun newInstance() = MoreReviewsFragment()
    }

    private lateinit var moreReviewViewModel: RatingAndReviewViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_more_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        arguments?.apply {
            val ratingAndResponse = Utils.jsonStringToObject(getString(KotlinUtils.REVIEW_STATISTICS),
                    RatingReviewResponse::class.java) as RatingReviewResponse
            setRatingDetailsUI(ratingAndResponse.reviewStatistics)
        }
    }

    override fun onStart() {
        super.onStart()
        setReviewsList()
    }

    private fun setupViewModel() {
        moreReviewViewModel =  ViewModelProvider(
                this,
                RatingAndReviewViewModelFactory(RatingAndReviewApiHelper(), "503780804")
        ).get(RatingAndReviewViewModel::class.java)
    }

    private fun setReviewsList() {
        val  moreReviewsAdapter = MoreReviewsAdapter(requireContext())
        rv_more_reviews.layoutManager = LinearLayoutManager(requireContext())
        rv_more_reviews.setHasFixedSize(true)
        rv_more_reviews.adapter = moreReviewsAdapter

        moreReviewsAdapter.addLoadStateListener {
            if (it.refresh == LoadState.Loading) {
                progress_bar?.visibility = View.VISIBLE
            } else {
                progress_bar?.visibility = View.GONE
            }
        }

        lifecycleScope.launch {
            moreReviewViewModel.reviewDataSource.collectLatest { pagedData ->
                moreReviewsAdapter.submitData(pagedData)
            }
        }

        moreReviewsAdapter.withLoadStateHeaderAndFooter(
                header = MoreReviewLoadStateAdapter(),
                footer = MoreReviewLoadStateAdapter()
        )
    }

    private fun setRatingDetailsUI(reviewStaticsData: ReviewStatistics) {
        layout_rating_details.close_top.visibility = View.GONE
        layout_rating_details.rating_details.text = getString(R.string.customer_reviews)
        layout_rating_details.pdpratings.visibility = View.VISIBLE
        reviewStaticsData.apply {
            layout_rating_details.recommend.text = recommendedPercentage
            layout_rating_details.rating_details.pdpratings.apply {
                ratingBarTop.rating = averageRating
                tvTotalReviews.text = "Customer Reviews"
            }
            recommend.text = recommendedPercentage
            view_2.visibility = View.GONE
            close.visibility = View.INVISIBLE
            setRatingDistributionUI(ratingDistribution, reviewCount)
        }
    }

    private fun setRatingDistributionUI(ratingDistribution: List<RatingDistribution>,
                                        reviewCount: Int) {
        for (rating in ratingDistribution) {
            when (rating.ratingValue) {
                1 -> {
                    progressbar_1.progress =
                            Utils.calculatePercentage(rating.count, reviewCount)
                    tv_1_starRating_count.text = rating.count.toString()
                }
                2 -> {
                    progressbar_2.progress =
                            Utils.calculatePercentage(rating.count, reviewCount)
                    tv_2_starRating_count.text = rating.count.toString()
                }
                3 -> {
                    progressbar_3.progress =
                            Utils.calculatePercentage(rating.count, reviewCount)
                    tv_3_starRating_count.text = rating.count.toString()
                }
                4 -> {
                    progressbar_4.progress =
                            Utils.calculatePercentage(rating.count, reviewCount)
                    tv_4_starRating_count.text = rating.count.toString()

                }
                5 -> {
                    progressbar_5.progress =
                            Utils.calculatePercentage(rating.count, reviewCount)
                    tv_5_starRating_count.text = rating.count.toString()
                }
            }
        }
    }
}