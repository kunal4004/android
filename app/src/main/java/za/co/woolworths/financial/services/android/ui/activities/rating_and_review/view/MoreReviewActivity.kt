package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_more_review.*
import kotlinx.android.synthetic.main.common_toolbar.view.*

class MoreReviewActivity : AppCompatActivity() {

    var reviewDetailsFragment: ReviewDetailsFragment? = null

    companion object{
        const val TAG = "ReviewDetailsFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviewer_info_details)
        reviewDetailsFragment = ReviewDetailsFragment.newInstance()
        toolbar.txt_toolbar_title.text = getString(R.string.review_details)
        toolbar.btn_back.setOnClickListener {
            super.onBackPressed()
        }
        goToReviewDetailsFragment()
    }

    private fun goToReviewDetailsFragment() {
        val fragmentManager: FragmentManager = getSupportFragmentManager()
        fragmentManager.beginTransaction()
                .replace(R.id.content_main_frame, reviewDetailsFragment!!, TAG).commit()
    }
}