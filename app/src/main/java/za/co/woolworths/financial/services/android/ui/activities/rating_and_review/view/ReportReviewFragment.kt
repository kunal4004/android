package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_report_review.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ReportReviewsAdapter
import za.co.woolworths.financial.services.android.util.KotlinUtils

class ReportReviewFragment: Fragment() , ReportReviewsAdapter.ReportItemClick {

    companion object {
        fun newInstance() = ReportReviewFragment()
    }

    var reportSuccessFragment: ReportSuccessFragment? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_report_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar?.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    fun init() {
        arguments?.apply {
            val reportReviews = getStringArrayList(KotlinUtils.REVIEW_REPORT)
            reportReviews?.let {
                setDefaultUi(reportReviews)
            }
        }
    }

     fun setDefaultUi(reportReviewList: List<String>) {
         val reportReviewsAdapter = ReportReviewsAdapter(reportReviewList, this)

         recyler_report?.apply {
             val linearLayoutManager = LinearLayoutManager(context)
             linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
             setLayoutManager(linearLayoutManager)
             adapter = reportReviewsAdapter
         }

        btn_submit_report.setOnClickListener {
            if (reportReviewsAdapter.getAllCheckBoxCount() !=0) {
                if (!edt_txt_feedback.isVisible) {
                    openReportScreenFragment()
                    return@setOnClickListener
                }
                if (edt_txt_feedback.isVisible && edt_txt_feedback.text.isNotEmpty()) {
                     openReportScreenFragment()
                }
            }
        }
    }

    private fun openReportScreenFragment() {
        activity?.apply {
            reportSuccessFragment = ReportSuccessFragment.newInstance()
            val fragmentManager = getSupportFragmentManager()
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.content_main_frame, reportSuccessFragment!!)
            fragmentTransaction.commit()
        }
    }

    override fun reportItemClicked(reportItem: String, isChecked: Boolean) {
        if (reportItem.equals("Others") && isChecked) {
            edt_txt_feedback.visibility = View.VISIBLE
            return
        } else if (reportItem.equals("Others") && !isChecked) {
            edt_txt_feedback.visibility = View.INVISIBLE
        }
    }
}
