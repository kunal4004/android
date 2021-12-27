package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.common_toolbar.view.*
import kotlinx.android.synthetic.main.fragment_more_reviews.*
import kotlinx.android.synthetic.main.no_connection_handler.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.SortOption
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingReviewResponse
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper.RatingAndReviewApiHelper
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.MoreReviewLoadStateAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.MoreReviewsAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModel
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModelFactory
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide.SkinProfileDialog
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import kotlinx.android.synthetic.main.no_connection_handler.view.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.MoreReviewHeaderAdapter
import java.util.ArrayList

class MoreReviewsFragment : Fragment(),
        MoreReviewsAdapter.ReviewItemClickListener,
        MoreReviewHeaderAdapter.SortAndRefineListener,
        MoreReviewLoadStateAdapter.HandlePaginationError {

    private var onSortRefineFragmentListener: OnSortRefineFragmentListener? = null
    private var sortString: String? = null
    private var refinementString: String? = null
    private var reportOptionsList: List<String> = listOf()

    companion object {
        fun newInstance() = MoreReviewsFragment()
    }

    private lateinit var moreReviewViewModel: RatingAndReviewViewModel
    private lateinit var ratingAndResponse: RatingReviewResponse

    var reportReviewFragment: ReportReviewFragment? = null

    var reviewDetailsFragment: ReviewDetailsFragment? = null

    private var productId: String = "-1"

    private var totalPages: Int = -1

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_more_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_more_review.btn_back.setOnClickListener {
            activity?.onBackPressed()
        }
        arguments?.apply {
            ratingAndResponse = Utils.jsonStringToObject(
                    getString(KotlinUtils.REVIEW_DATA),
                    RatingReviewResponse::class.java
            ) as RatingReviewResponse

            productId = ratingAndResponse.reviews.get(0).productId
            reportOptionsList = ratingAndResponse.reportReviewOptions
            setupViewModel()
            setReviewsList(null, null, reportOptionsList)
            btnRetry?.setOnClickListener {
                setReviewsList(null, null, reportOptionsList)
            }
        }
    }

    private fun setupViewModel() {
        moreReviewViewModel = ViewModelProvider(
                this,
                RatingAndReviewViewModelFactory(RatingAndReviewApiHelper())
        ).get(RatingAndReviewViewModel::class.java)
    }

    private fun setReviewsList(
            sort: String?,
            refinements: String?,
            reportReviewOptions: List<String>?
    ) {

        val moreReviewsAdapter = MoreReviewsAdapter(
                requireContext(),
                this,
                reportReviewOptions,
                0
        )

        val loadStateAdapter =  moreReviewsAdapter.withLoadStateFooter(
                footer = MoreReviewLoadStateAdapter({
                    moreReviewsAdapter.retry()
                }, this@MoreReviewsFragment)
        )

        val headerAdapter = MoreReviewHeaderAdapter(
                ratingAndResponse.reviewStatistics,
                this)
        val concatAdapter = ConcatAdapter(headerAdapter, moreReviewsAdapter, loadStateAdapter)

        rv_more_reviews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = concatAdapter
        }

        lifecycleScope.launch {
            moreReviewViewModel.getReviewDataSource(
                    productId,
                    sort, refinements
            ).collectLatest { pagedData ->
                moreReviewViewModel.getRatingReviewResponseLiveData().observe(
                        requireActivity(), {
                    totalPages = it.totalResults
                    moreReviewsAdapter.setTotalPages(totalPages)
                })
                moreReviewsAdapter.submitData(lifecycle, pagedData)
                moreReviewsAdapter.notifyDataSetChanged()
            }
        }

        moreReviewsAdapter.addLoadStateListener {
            if (it.refresh == LoadState.Loading) {
                progress_bar?.visibility = View.VISIBLE
            } else {
                // show error
                progress_bar?.visibility = View.GONE
                when {
                    it.prepend is LoadState.Error -> it.prepend as LoadState.Error
                    it.append is LoadState.Error -> it.append as LoadState.Error
                    it.refresh is LoadState.Error -> {
                        it.refresh as LoadState.Error
                        error_layout?.visibility = View.VISIBLE
                        error_layout?.no_connection_layout?.visibility = View.VISIBLE
                    }
                    else -> null
                }
            }
        }
    }

    override fun openSkinProfileDialog(reviews: Reviews) {
        viewSkinProfileDialog(reviews)
    }

    override fun openReportScreen(reportReviewOptions: List<String>?) {
        val bundle = Bundle()
        bundle.putStringArrayList(
                KotlinUtils.REVIEW_REPORT,
                reportReviewOptions as ArrayList<String>
        )
        reportReviewFragment = ReportReviewFragment.newInstance()
        reportReviewFragment?.arguments = bundle
        navigateToNextScreen(reportReviewFragment)
    }

    fun navigateToNextScreen(fragment: Fragment?) {
        activity?.apply {
            fragment?.let {
                val fragmentManager = supportFragmentManager
                val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.content_main_frame, fragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
        }
    }

    override fun openReviewDetailsScreen(reviews: Reviews, reportReviewOptions: List<String>?) {
        RatingAndReviewUtil.isComingFromMoreReview = true
        val bundle = Bundle()
        bundle.putStringArrayList(
                KotlinUtils.REVIEW_REPORT,
                reportReviewOptions as ArrayList<String>
        )
        bundle.putSerializable(KotlinUtils.REVIEW_DATA, reviews)
        reviewDetailsFragment = ReviewDetailsFragment.newInstance()
        reviewDetailsFragment?.arguments = bundle
        navigateToNextScreen(reviewDetailsFragment)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSortRefineFragmentListener) {
            onSortRefineFragmentListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onSortRefineFragmentListener = null
    }

    interface OnSortRefineFragmentListener {
        fun openDrawer()
        fun closeDrawer()
        fun setupDrawer(isShortClicked: Boolean, ratingReviewResponse: RatingReviewResponse)
    }

    override fun openRefineDrawer() {
        val data = moreReviewViewModel.getRatingReviewResponseLiveData()
        data.value?.let {
            onSortRefineFragmentListener?.setupDrawer(false, it)
            onSortRefineFragmentListener?.openDrawer()
        }
    }

    override fun openSortDrawer() {
        val data = moreReviewViewModel.getRatingReviewResponseLiveData()
        data.value?.let {
            onSortRefineFragmentListener?.setupDrawer(true, it)
            onSortRefineFragmentListener?.openDrawer()
        }

    }

    fun onSortOptionSelected(sortOption: SortOption) {
        sortString = sortOption.sortOption
        onSortRefineFragmentListener?.closeDrawer()
        setReviewsList(sortString, refinementString, reportOptionsList)
    }

    fun onRefineOptionSelected(refinements: String?) {
        if (refinementString != refinements) {
            refinementString = refinements
            setReviewsList(sortString, refinementString, reportOptionsList)
        }
        onSortRefineFragmentListener?.closeDrawer()
    }

    override fun showFooterErrorMessage() {
        val actionTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        Snackbar.make(more_review_layout, R.string.failed_more_reviews, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry_txt, {
                    setReviewsList(null, null, reportOptionsList)
                }).setActionTextColor(actionTextColor)
                .show()
    }
}
