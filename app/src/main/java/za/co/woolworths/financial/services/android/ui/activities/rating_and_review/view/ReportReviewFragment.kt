package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

class ReportReviewFragment : Fragment(), ReportReviewsAdapter.ReportItemClick, TextWatcher {

    companion object {
        fun newInstance() = ReportReviewFragment()
        private const val OTHERS = "Others"
    }

    private lateinit var reportReviewsAdapter: ReportReviewsAdapter
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
        arguments?.apply {
            val reportReviews = getStringArrayList(KotlinUtils.REVIEW_REPORT)
            reportReviews?.let {
                setDefaultUi(reportReviews)
            }
        }
    }

    private fun setDefaultUi(reportReviewList: List<String>) {
        val llm = LinearLayoutManager(requireContext())
        llm.orientation = LinearLayoutManager.VERTICAL
        recyler_report.setLayoutManager(llm)
        reportReviewsAdapter = ReportReviewsAdapter(reportReviewList, this)
        recyler_report.setAdapter(reportReviewsAdapter)
        edt_txt_feedback?.addTextChangedListener(this)
        btn_submit_report.setOnClickListener {
            openReportScreenFragment()
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

        if (reportItem.equals(OTHERS) && isChecked) {
            edt_txt_feedback.visibility = View.VISIBLE
        } else if (reportItem.equals(OTHERS) && !isChecked) {
            edt_txt_feedback.visibility = View.INVISIBLE
        }

        if (reportReviewsAdapter.getAllCheckBoxCount() != 0) {
            if (edt_txt_feedback?.isVisible == true) {
                if (edt_txt_feedback?.text.toString().isEmpty()) {
                    btn_submit_report.setBackgroundColor(resources.getColor(R.color.gray))
                    btn_submit_report.isEnabled = false
                }
            } else {
                btn_submit_report.setBackgroundColor(resources.getColor(R.color.black))
                btn_submit_report.isEnabled = true
            }
        } else {
            btn_submit_report.setBackgroundColor(resources.getColor(R.color.gray))
            btn_submit_report.isEnabled = false
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no  need to implement
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no  need to implement
    }

    override fun afterTextChanged(s: Editable?) {
        if (s.toString().isEmpty()) {
            btn_submit_report.setBackgroundColor(resources.getColor(R.color.gray))
            btn_submit_report.isEnabled = false
        } else {
            btn_submit_report.setBackgroundColor(resources.getColor(R.color.black))
            btn_submit_report.isEnabled = true
        }
    }
}
