package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_report_review.*
import kotlinx.android.synthetic.main.review_detail_layout.*
import kotlinx.android.synthetic.main.skin_profile_layout.view.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ReportReviewsAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.SkinProfileAdapter
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.util.ArrayList

class ReportReviewFragment: Fragment() , ReportReviewsAdapter.ReportItemClick {

    companion object {
        fun newInstance() = ReportReviewFragment()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_report_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        val reportReviewsAdapter = ReportReviewsAdapter(reportReviewList, this)
        recyler_report.setAdapter(reportReviewsAdapter)
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
