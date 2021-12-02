package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.common_toolbar.view.*
import kotlinx.android.synthetic.main.fragment_more_reviews.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.SortOption
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingReviewResponse
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper.RatingAndReviewApiHelper
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.MoreReviewLoadStateAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.MoreReviewsAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModel
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModelFactory
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide.SkinProfileDialog
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

class MoreReviewsFragment : Fragment(), MoreReviewsAdapter.ReviewItemClickListener,
    MoreReviewsAdapter.SortAndRefineListener {
    private var onSortRefineFragmentListener: OnSortRefineFragmentListener? = null
    private var sortString: String? = null
    private var refinementString: String? = null

    companion object {
        fun newInstance() = MoreReviewsFragment()
    }

    private lateinit var moreReviewViewModel: RatingAndReviewViewModel
    private lateinit var ratingAndResponse: RatingReviewResponse

    var reportReviewFragment: ReportReviewFragment? = null


    private var productId: String = "-1"

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
            setupViewModel()
            setReviewsList(null, null, null)
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
            ratingAndResponse.reviewStatistics,
            reportReviewOptions,
            this
        )

        moreReviewsAdapter.addLoadStateListener {
            if (it.refresh == LoadState.Loading) {
                progress_bar?.visibility = View.VISIBLE
            } else {
                progress_bar?.visibility = View.GONE
            }
        }
        moreReviewsAdapter.withLoadStateFooter(
            footer = MoreReviewLoadStateAdapter()
        )
        lifecycleScope.launch {
            moreReviewViewModel.getReviewDataSource(
                productId,
                sort, refinements
            ).collectLatest { pagedData ->
                moreReviewsAdapter.submitData(pagedData)
            }
        }

        rv_more_reviews.layoutManager = LinearLayoutManager(requireContext())
        rv_more_reviews.adapter = moreReviewsAdapter
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
        onSortRefineFragmentListener?.setupDrawer(false, ratingAndResponse)
        onSortRefineFragmentListener?.openDrawer()
    }

    override fun openSortDrawer() {
        val data = moreReviewViewModel.getRatingReviewResponseLiveData()
        Log.e("data_is:", data.value.toString())
        onSortRefineFragmentListener?.setupDrawer(true, data.value!!)
        onSortRefineFragmentListener?.openDrawer()
    }

    fun onSortOptionSelected(sortOption: SortOption) {
        sortString = sortOption.sortOption
        onSortRefineFragmentListener?.closeDrawer()
        setReviewsList(sortString, refinementString, null)
    }

    fun onRefineOptionSelected(refinements: String?) {
        refinementString = refinements
        onSortRefineFragmentListener?.closeDrawer()
        if (refinementString != null)
            setReviewsList(sortString, refinementString, null)
    }
}
