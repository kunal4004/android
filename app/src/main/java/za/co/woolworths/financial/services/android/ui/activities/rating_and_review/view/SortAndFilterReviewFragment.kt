package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.SortOption
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingReviewResponse
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ReviewSortOptionsAdapter


class SortAndFilterReviewFragment : Fragment(), ReviewSortOptionsAdapter.OnSortOptionSelected {
    private var toolbarTitle: TextView? = null
    private var rvSortOptions: RecyclerView? = null

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
    }

    fun setDrawerUI(isShortClicked: Boolean, ratingReviewResponse: RatingReviewResponse) {
        if (isShortClicked) {
            toolbarTitle?.text = getString(R.string.sort_by)
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

        }

    }

    override fun onSortOptionSelected(sortOption: SortOption) {
        Toast.makeText(context, sortOption.label, Toast.LENGTH_LONG).show()
        val fm = fragmentManager
        val fragm: MoreReviewsFragment? =
            fm!!.findFragmentById(R.id.content_main_frame) as MoreReviewsFragment?
        fragm?.onSortOptionSelected(sortOption)
    }


}