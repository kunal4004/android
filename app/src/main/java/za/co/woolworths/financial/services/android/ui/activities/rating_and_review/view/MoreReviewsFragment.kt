package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_more_reviews.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingReviewResponse
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.ReviewStatistics
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper.RatingAndReviewApiHelper
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.MoreReviewLoadStateAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.MoreReviewsAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModel
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModelFactory
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide.SkinProfileDialog
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import androidx.fragment.app.FragmentTransaction
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.common_toolbar.view.*
import java.util.ArrayList

class MoreReviewsFragment : Fragment(), MoreReviewsAdapter.ReviewItemClickListener {

    companion object {
        fun newInstance() = MoreReviewsFragment()
    }

    private lateinit var moreReviewViewModel: RatingAndReviewViewModel

    var reportReviewFragment: ReportReviewFragment? = null

    private var productId: String = "-1"

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_more_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_more_review.btn_back.setOnClickListener {
            activity?.onBackPressed()
        }
        arguments?.apply {
            val ratingAndResponse = Utils.jsonStringToObject(getString(KotlinUtils.REVIEW_DATA),
                    RatingReviewResponse::class.java) as RatingReviewResponse
            productId = ratingAndResponse.reviews.get(0).productId
            setupViewModel()
            setReviewsList(ratingAndResponse.reviewStatistics, ratingAndResponse.reportReviewOptions)
        }
    }

    private fun setupViewModel() {
        moreReviewViewModel = ViewModelProvider(
                this,
                RatingAndReviewViewModelFactory(RatingAndReviewApiHelper())
        ).get(RatingAndReviewViewModel::class.java)
    }

    private fun setReviewsList(reviewStatistics: ReviewStatistics,
                               reportReviewOptions: List<String>) {
        val moreReviewsAdapter = MoreReviewsAdapter(requireContext(),
                this, reviewStatistics, reportReviewOptions)
        rv_more_reviews.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moreReviewsAdapter.withLoadStateFooter(
                    footer = MoreReviewLoadStateAdapter{moreReviewsAdapter.retry()}
            )
        }

        lifecycleScope.launch {
            moreReviewViewModel.getReviewDataSource(productId).collectLatest { pagedData ->
                moreReviewsAdapter.submitData(pagedData)
            }
        }

        moreReviewsAdapter.addLoadStateListener {
            if (it.refresh == LoadState.Loading) {
                progress_bar?.visibility = View.VISIBLE
            } else {
                progress_bar?.visibility = View.GONE
            }
        }
    }

    override fun openSkinProfileDialog(reviews: Reviews) {
        viewSkinProfileDialog(reviews)
    }

    override fun openReportScreen(reportReviewOptions: List<String>) {
        val bundle = Bundle()
        bundle.putStringArrayList(KotlinUtils.REVIEW_REPORT, reportReviewOptions as ArrayList<String>)
        reportReviewFragment = ReportReviewFragment.newInstance()
        reportReviewFragment?.arguments = bundle
        activity?.apply {
            val fragmentManager = getSupportFragmentManager()
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.content_main_frame, reportReviewFragment!!)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    private fun viewSkinProfileDialog(reviews: Reviews) {
        val dialog = SkinProfileDialog(reviews)
        activity?.apply {
            childFragmentManager.beginTransaction()
                    .let { fragmentTransaction ->
                        dialog.show(
                                fragmentTransaction,
                                SkinProfileDialog::class.java.simpleName
                        )
                    }
        }
    }
}