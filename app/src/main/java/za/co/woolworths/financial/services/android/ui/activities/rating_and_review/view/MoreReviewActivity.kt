package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_reviewer_info_details.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingReviewResponse

class MoreReviewActivity : AppCompatActivity(),
        MoreReviewsFragment.OnSortRefineFragmentListener {

    private var moreReviewsFragment: MoreReviewsFragment? = null
    private var sortAndFilterReviewFragment: SortAndFilterReviewFragment? = null

    companion object {
        const val TAG = "ReviewDetailsFragment"
        const val SORT_AND_FILTER_FRAGMENT ="SortAndFilterFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviewer_info_details)
        moreReviewsFragment = MoreReviewsFragment.newInstance()
        sortAndFilterReviewFragment = SortAndFilterReviewFragment.newInstance()
        if (intent.extras != null)
            goToMoreReviewsFragment(intent.extras)
    }

    private fun goToMoreReviewsFragment(bundle: Bundle?) {
        moreReviewsFragment?.arguments = bundle
        val fragmentManager: FragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.content_main_frame, moreReviewsFragment!!, TAG).commit()

        fragmentManager.beginTransaction()
            .replace(R.id.drawer_fragment, sortAndFilterReviewFragment!!,
                SORT_AND_FILTER_FRAGMENT).commit()

    }

    override fun openDrawer() {
        dlSortRefine.openDrawer(Gravity.RIGHT)
    }

    override fun closeDrawer() {
        dlSortRefine.closeDrawers()
    }

    override fun setupDrawer(isShortClicked: Boolean, ratingReviewResponse: RatingReviewResponse) {
        sortAndFilterReviewFragment?.setDrawerUI(isShortClicked,ratingReviewResponse)
    }
}