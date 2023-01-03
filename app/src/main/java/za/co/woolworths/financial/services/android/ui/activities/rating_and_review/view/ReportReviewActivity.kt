package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityReportReviewBinding
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Reviews
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class ReportReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportReviewBinding
    var reportReviewFragment: ReportReviewFragment? = null

    companion object{
        const val TAG = "ReportReviewFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reportReviewFragment = ReportReviewFragment.newInstance()
        if (intent != null)
            //goToReportReviewFragment(intent.extras?.getStringArrayList(KotlinUtils.REVIEW_REPORT) as ArrayList<String>, intent.extras?.get(KotlinUtils.REVIEW_DATA) as Reviews)
            intent.extras?.let { goToReportReviewFragment(it) }
    }

    private fun goToReportReviewFragment(bundle: Bundle) {
        val b = Bundle()
        b.putStringArrayList(KotlinUtils.REVIEW_REPORT, bundle.getStringArrayList(KotlinUtils.REVIEW_REPORT))
        b.putSerializable(KotlinUtils.REVIEW_DATA, Utils.jsonStringToObject(bundle.getString(KotlinUtils.REVIEW_DATA), Reviews::class.java) as Reviews)
        reportReviewFragment?.arguments = b
        val fragmentManager: FragmentManager = getSupportFragmentManager()
        fragmentManager.beginTransaction()
                .replace(R.id.content_main_frame, reportReviewFragment!!, TAG).commit()
    }
}