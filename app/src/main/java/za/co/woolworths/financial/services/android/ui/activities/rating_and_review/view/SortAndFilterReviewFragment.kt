package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_refinement.*
import za.co.woolworths.financial.services.android.models.dto.SortOption
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingReviewResponse
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.Refinements
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ReviewRefineOptionsAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ReviewSortOptionsAdapter


class SortAndFilterReviewFragment : Fragment(), ReviewSortOptionsAdapter.OnSortOptionSelected, ReviewRefineOptionsAdapter.OnRefineOptionSelected {
    private var toolbarTitle: TextView? = null
    private var rvSortOptions: RecyclerView? = null
    private var refineAdapter: ReviewRefineOptionsAdapter? = null
    private var btSeeResult: RelativeLayout? = null
    private var tvClearFilter: TextView? = null
    companion object {
        fun newInstance() =
            SortAndFilterReviewFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sort_and_filter_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarTitle = view.findViewById(R.id.txt_toolbar_title)
        rvSortOptions = view.findViewById(R.id.sortOptionsList)
        btSeeResult = view.findViewById(R.id.refinementSeeResult)
        tvClearFilter = view.findViewById(R.id.clearAndResetFilter)

        btSeeResult?.setOnClickListener(View.OnClickListener {
            onRefineOptionSelected1(refineAdapter?.getRefineOption()) })

        tvClearFilter?.setOnClickListener(View.OnClickListener { refineAdapter?.clearRefinement() })
    }

    fun setDrawerUI(isShortClicked: Boolean, ratingReviewResponse: RatingReviewResponse) {
        if (isShortClicked) {
            toolbarTitle?.text = getString(R.string.sort_by)
            btSeeResult?.visibility = View.INVISIBLE
            tvClearFilter?.visibility = View.INVISIBLE
            rvSortOptions?.layoutManager =
                activity?.let { activity -> LinearLayoutManager(activity) }
             rvSortOptions?.adapter = activity?.let { activity ->
                ReviewSortOptionsAdapter(
                    activity,
                    ratingReviewResponse.sortOptions,
                    this
                )
            }
        } else {
            toolbarTitle?.text = getString(R.string.refine)
            btSeeResult?.visibility = View.VISIBLE
            tvClearFilter?.visibility = View.VISIBLE
            rvSortOptions?.layoutManager =
                activity?.let { activity -> LinearLayoutManager(activity) }
            refineAdapter = activity?.let { activity ->
                ReviewRefineOptionsAdapter(
                    activity,
                    ratingReviewResponse.refinements,
                    this
                )
            }
            rvSortOptions?.adapter = refineAdapter
        }

    }

    override fun onSortOptionSelected(sortOption: SortOption) {
        val fm = fragmentManager
        val fragm: MoreReviewsFragment? =
            fm!!.findFragmentById(R.id.content_main_frame) as MoreReviewsFragment?
        fragm?.onSortOptionSelected(sortOption)
    }

    override fun onRefineOptionSelected(refinementOption: Refinements) {
        TODO("Not yet implemented")
    }

    fun onRefineOptionSelected1(refinementOption: String?){
        val fm = fragmentManager
        val fragm: MoreReviewsFragment? =
            fm!!.findFragmentById(R.id.content_main_frame) as MoreReviewsFragment?
        fragm?.onRefineOptionSelected(refinementOption)
    }

}