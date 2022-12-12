package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityReviewerInfoDetailsBinding
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingReviewResponse

class MoreReviewActivity : AppCompatActivity(),
        MoreReviewsFragment.OnSortRefineFragmentListener {

    private lateinit var binding: ActivityReviewerInfoDetailsBinding
    private var moreReviewsFragment: MoreReviewsFragment? = null
    private var sortAndFilterReviewFragment: SortAndFilterReviewFragment? = null

    companion object {
        const val TAG = "ReviewDetailsFragment"
        const val SORT_AND_FILTER_FRAGMENT ="SortAndFilterFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewerInfoDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.dlSortRefine.openDrawer(Gravity.RIGHT)
    }

    override fun closeDrawer() {
        binding.dlSortRefine.closeDrawers()
    }

    override fun setupDrawer(isShortClicked: Boolean, ratingReviewResponse: RatingReviewResponse) {
        sortAndFilterReviewFragment?.setDrawerUI(isShortClicked,ratingReviewResponse)
    }
}