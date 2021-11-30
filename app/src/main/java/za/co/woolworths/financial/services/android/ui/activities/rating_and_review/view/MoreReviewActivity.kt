package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_more_review.*
import kotlinx.android.synthetic.main.activity_more_review.toolbar
import kotlinx.android.synthetic.main.activity_reviewer_info_details.*
import kotlinx.android.synthetic.main.common_toolbar.view.*
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingReviewResponse
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.fragments.RefinementDrawerFragment


class MoreReviewActivity : AppCompatActivity(), MoreReviewsFragment.OnSortRefineFragmentListener {

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
        toolbar.btn_back.setOnClickListener {
            super.onBackPressed()
        }
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