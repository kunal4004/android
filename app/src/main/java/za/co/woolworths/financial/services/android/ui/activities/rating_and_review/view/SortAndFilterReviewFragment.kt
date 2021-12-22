package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.common_toolbar.view.*
import kotlinx.android.synthetic.main.fragment_sort_and_filter_review.*
import za.co.woolworths.financial.services.android.models.dto.SortOption
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingReviewResponse
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ReviewRefineOptionsAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ReviewSortOptionsAdapter


class SortAndFilterReviewFragment : Fragment(),
        ReviewSortOptionsAdapter.OnSortOptionSelected {
    private var onSortRefineFragmentListener: MoreReviewsFragment.OnSortRefineFragmentListener? = null
    private var toolbarTitle: TextView? = null
    private var rvSortOptions: RecyclerView? = null
    private var refineAdapter: ReviewRefineOptionsAdapter? = null
    private var btSeeResult: RelativeLayout? = null
    private var tvClearFilter: TextView? = null

    companion object {
        fun newInstance() =
            SortAndFilterReviewFragment()
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
            onRefineOptionSelected(refineAdapter?.getRefineOption())
        })

        tvClearFilter?.setOnClickListener(View.OnClickListener { refineAdapter?.clearRefinement() })
        toolbar.btn_back.setOnClickListener(View.OnClickListener {
            onSortRefineFragmentListener?.closeDrawer() })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MoreReviewsFragment.OnSortRefineFragmentListener) {
            onSortRefineFragmentListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onSortRefineFragmentListener = null
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
            toolbarTitle?.text = getString(R.string.filter)
            btSeeResult?.visibility = View.VISIBLE
            tvClearFilter?.visibility = View.VISIBLE
            rvSortOptions?.layoutManager =
                activity?.let { activity -> LinearLayoutManager(activity) }
            refineAdapter = activity?.let { activity ->
                ReviewRefineOptionsAdapter(
                    activity,
                    ratingReviewResponse.refinements
                )
            }
            rvSortOptions?.adapter = refineAdapter
        }

    }

    override fun onSortOptionSelected(sortOption: SortOption) {
        val moreReviewsFragment: MoreReviewsFragment =
            requireFragmentManager().findFragmentById(R.id.content_main_frame) as MoreReviewsFragment
        moreReviewsFragment.onSortOptionSelected(sortOption)
    }

    private fun onRefineOptionSelected(refinementOption: String?) {
        val moreReviewsFragment: MoreReviewsFragment =
            requireFragmentManager().findFragmentById(R.id.content_main_frame) as MoreReviewsFragment
        moreReviewsFragment.onRefineOptionSelected(refinementOption)
    }

}