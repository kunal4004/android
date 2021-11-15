package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_more_review.*
import kotlinx.android.synthetic.main.common_toolbar.view.*
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingReviewResponse
import java.io.Serializable

class MoreReviewActivity : AppCompatActivity() {

    var moreReviewsFragment: MoreReviewsFragment? = null

    companion object{
        const val TAG = "ReviewDetailsFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviewer_info_details)
        moreReviewsFragment = MoreReviewsFragment.newInstance()
        toolbar.txt_toolbar_title.text = getString(R.string.review_details)
        toolbar.btn_back.setOnClickListener {
            super.onBackPressed()
        }
        if (intent.extras != null)
            goToMoreReviewsFragment(intent.extras)
    }

    private fun goToMoreReviewsFragment(bundle: Bundle?) {
        moreReviewsFragment?.arguments = bundle
        val fragmentManager: FragmentManager = getSupportFragmentManager()
        fragmentManager.beginTransaction()
                .replace(R.id.content_main_frame, moreReviewsFragment!!, TAG).commit()
    }
}