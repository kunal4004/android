package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_more_reviews.*
import kotlinx.android.synthetic.main.fragment_more_reviews.view.*
import kotlinx.android.synthetic.main.pdp_rating_layout.*
import kotlinx.android.synthetic.main.ratings_ratingdetails.*
import kotlinx.android.synthetic.main.ratings_ratingdetails.view.*
import kotlinx.android.synthetic.main.sort_and_refine_selection_layout.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.SortOption
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingDistribution
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingReviewResponse
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.ReviewStatistics
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.newtwork.apihelper.RatingAndReviewApiHelper
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.MoreReviewLoadStateAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.MoreReviewsAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModel
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModelFactory
import za.co.woolworths.financial.services.android.ui.adapters.SortOptionsAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide.SkinProfileDialog
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.tooltip.ViewTooltip

class MoreReviewsFragment : Fragment(), MoreReviewsAdapter.SkinProfileDialogOpenListener, View.OnClickListener, SortOptionsAdapter.OnSortOptionSelected {
    private var sortOptionDialog: Dialog? = null
    private var flReview: FrameLayout? = null

    companion object {
        fun newInstance() = MoreReviewsFragment()
    }

    private lateinit var moreReviewViewModel: RatingAndReviewViewModel
    private lateinit var ratingAndResponse: RatingReviewResponse

    private var productId: String = "-1"

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_more_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        flReview = view.findViewById(R.id.fl_review)
        arguments?.apply {
              ratingAndResponse = Utils.jsonStringToObject(getString(KotlinUtils.REVIEW_STATISTICS),
                    RatingReviewResponse::class.java) as RatingReviewResponse
            productId = ratingAndResponse.reviews.get(0).productId
            setupViewModel()
            setReviewsList(null,null,ratingAndResponse.reviewStatistics)
        }
        refineProducts?.setOnClickListener(this@MoreReviewsFragment)
        sortProducts?.setOnClickListener(this@MoreReviewsFragment)
        flReview?.setOnClickListener(this@MoreReviewsFragment)
    }

    override fun onStart() {
        super.onStart()
        //setReviewsList(null,null, ratingAndResponse.reviewStatistics)
        }

    private fun setupViewModel() {
        moreReviewViewModel = ViewModelProvider(
                this,
                RatingAndReviewViewModelFactory(RatingAndReviewApiHelper())
        ).get(RatingAndReviewViewModel::class.java)
    }

    private fun setReviewsList(sort: String?, refinements: String?,reviewStatistics: ReviewStatistics) {
        /*val moreReviewsAdapter = MoreReviewsAdapter(requireContext(), this)
    private fun setReviewsList(reviewStatistics: ReviewStatistics) {*/
        val moreReviewsAdapter = MoreReviewsAdapter(requireContext(), this, reviewStatistics)
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
            moreReviewViewModel.getReviewDataSource(productId, sort, refinements).collectLatest { pagedData ->
                moreReviewsAdapter.submitData(pagedData)
            }
        }


        rv_more_reviews.layoutManager = LinearLayoutManager(requireContext())
        rv_more_reviews.adapter = moreReviewsAdapter
    }

    private fun setRatingDetailsUI(reviewStaticsData: ReviewStatistics) {
        layout_rating_details.close_top.visibility = View.GONE
        layout_rating_details.rating_details.text = getString(R.string.customer_reviews)
        layout_rating_details.pdpratings.visibility = View.VISIBLE
        reviewStaticsData.apply {
            layout_rating_details.recommend.text = recommendedPercentage
            layout_rating_details.rating_details.pdpratings.apply {
                ratingBarTop.rating = averageRating
                tvTotalReviews.text = getString(R.string.customer_reviews)
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

    override fun onClick(view: View) {
        KotlinUtils.avoidDoubleClicks(view)
        //activity?.let { activity ->
            when (view.id) {
                R.id.refineProducts -> {
                    //Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.REFINE_EVENT_APPEARED, activity)
                    (activity as MoreReviewActivity).let {
                        //it.setUpDrawerFragment(productView, productRequestBody)
                        //it.openDrawerFragment()
                    }
                }
                R.id.sortProducts -> {
                    //Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SORTBY_EVENT_APPEARED, activity)
                    ratingAndResponse.sortOptions.let {
                        val rectF = Rect()
                        sortProducts.getGlobalVisibleRect(rectF)
                        this.showShortOptions(it,rectF.bottom)
                    }
                }
                R.id.fl_review -> {
                    if (sortOptionDialog != null && sortOptionDialog?.isShowing == true)
                        sortOptionDialog?.dismiss()
                }
                else -> return
        }
    }

    override fun onSortOptionSelected(sortOption: SortOption) {
        if (sortOptionDialog != null && sortOptionDialog?.isShowing == true) {
            sortOptionDialog?.dismiss()
            //val arguments = HashMap<String, String>()
            //arguments[FirebaseManagerAnalyticsProperties.PropertyNames.SORT_OPTION_NAME] = sortOption.label
            //activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SORTBY_EVENT_APPLIED, arguments, this) }
            //arguments[FirebaseManagerAnalyticsProperties.PropertyNames.SORT_OPTION_NAME] =
            //    sortOption.label
            /*activity?.apply {  Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SORTBY_EVENT_APPLIED,
                arguments,this
            )}*/
            //updateProductRequestBodyForSort(sortOption.sortOption)
            //reloadProductsWithSortAndFilter()
            //setReviewsList(sortOption.sortOption,null)

        }
    }


    @SuppressLint("InflateParams")
    private fun showShortOptions(sortOptions: ArrayList<SortOption>, position: Int) {
        sortOptionDialog = activity?.let { activity -> Dialog(activity) }
        sortOptionDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.sort_options_view, null)
            val rcvSortOptions = view.findViewById<RecyclerView>(R.id.sortOptionsList)
            rcvSortOptions?.layoutManager =
                activity?.let { activity -> LinearLayoutManager(activity) }
            rcvSortOptions?.adapter = activity?.let { activity ->
                SortOptionsAdapter(
                    activity,
                    sortOptions,
                    this@MoreReviewsFragment
                )
            }
            setContentView(view)
            window?.apply {
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                val param :WindowManager.LayoutParams = attributes
                param.gravity = Gravity.TOP
                param.y = (position- 330)
                setBackgroundDrawableResource(R.color.transparent)
            }

            setTitle(null)
            setCancelable(true)
            flReview?.visibility = View.VISIBLE
            show()

        }
        sortOptionDialog?.setOnDismissListener {
            flReview?.visibility = View.GONE
        }

    }

    override fun openSkinProfileDialog(reviews: Reviews) {
        viewSkinProfileDialog(reviews)
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
