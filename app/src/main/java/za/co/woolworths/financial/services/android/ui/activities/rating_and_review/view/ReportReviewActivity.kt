package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_more_review.*
import kotlinx.android.synthetic.main.common_toolbar.view.*
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.io.Serializable

class ReportReviewActivity : AppCompatActivity() {
    var reportReviewFragment: ReportReviewFragment? = null

    companion object{
        const val TAG = "ReportReviewFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_review)
        reportReviewFragment = ReportReviewFragment.newInstance()
        toolbar.btn_back.setOnClickListener {
            super.onBackPressed()
        }
        if (intent != null)
            goToReportReviewFragment(intent.getSerializableExtra(KotlinUtils.REVIEW_REPORT) as ArrayList<String>)
    }

    private fun goToReportReviewFragment(reportList: ArrayList<String>) {
        val bundle = Bundle()
        bundle.putStringArrayList(KotlinUtils.REVIEW_REPORT, reportList)
        reportReviewFragment?.arguments = bundle
        val fragmentManager: FragmentManager = getSupportFragmentManager()
        fragmentManager.beginTransaction()
                .replace(R.id.content_main_frame, reportReviewFragment!!, TAG).commit()
    }
}