package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentSortAndFilterReviewBinding
import za.co.woolworths.financial.services.android.models.dto.SortOption
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingReviewResponse
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ReviewRefineOptionsAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ReviewSortOptionsAdapter
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class SortAndFilterReviewFragment : BaseFragmentBinding<FragmentSortAndFilterReviewBinding>(FragmentSortAndFilterReviewBinding::inflate),
        ReviewSortOptionsAdapter.OnSortOptionSelected {
    private var onSortRefineFragmentListener: MoreReviewsFragment.OnSortRefineFragmentListener? = null
    private var refineAdapter: ReviewRefineOptionsAdapter? = null

    companion object {
        fun newInstance() =
            SortAndFilterReviewFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.refinementSeeResult.setOnClickListener {
            onRefineOptionSelected(refineAdapter?.getRefineOption())
        }

        binding.clearAndResetFilter.setOnClickListener {
            refineAdapter?.clearRefinement()
        }
        binding.toolbar.btnBack.setOnClickListener {
            onSortRefineFragmentListener?.closeDrawer()
        }
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
            binding.toolbar.txtToolbarTitle.text = getString(R.string.sort_by)
            binding.refinementSeeResult.visibility = View.INVISIBLE
            binding.clearAndResetFilter.visibility = View.INVISIBLE
            binding.sortOptionsList.layoutManager =
                activity?.let { activity -> LinearLayoutManager(activity) }
            binding.sortOptionsList.adapter = activity?.let { activity ->
                ReviewSortOptionsAdapter(
                    activity,
                    ratingReviewResponse.sortOptions,
                    this
                )
            }
        } else {
            binding.toolbar.txtToolbarTitle.text = getString(R.string.filter)
            binding.refinementSeeResult.visibility = View.VISIBLE
            binding.clearAndResetFilter.visibility = View.VISIBLE
            binding.sortOptionsList.layoutManager =
                activity?.let { activity -> LinearLayoutManager(activity) }
            refineAdapter = activity?.let { activity ->
                ReviewRefineOptionsAdapter(
                    activity,
                    ratingReviewResponse.refinements
                )
            }
            binding.sortOptionsList.adapter = refineAdapter
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