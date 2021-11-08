package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_more_review.*
import kotlinx.android.synthetic.main.common_toolbar.view.*

class MoreReviewActivity : AppCompatActivity() {

    var moreReviewFragment: MoreReviewFragment? = null

    companion object {
        const val TAG = "MoreReviewFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_review)
        moreReviewFragment = MoreReviewFragment()
        tool_bar.btn_back.setOnClickListener {
            super.onBackPressed()
        }
        navigateToMoreReviewScreen()
    }

    private fun navigateToMoreReviewScreen() {
        val fragmentManager: FragmentManager = getSupportFragmentManager()
        fragmentManager.beginTransaction()
                .replace(R.id.content_main_frame, moreReviewFragment!!,
                        TAG).commit()
    }
}