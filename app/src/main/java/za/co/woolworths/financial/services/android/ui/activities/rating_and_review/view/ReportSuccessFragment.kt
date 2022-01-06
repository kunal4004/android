package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_report_review_success.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil

class ReportSuccessFragment : Fragment() {

    companion object {
        fun newInstance() = ReportSuccessFragment()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_report_review_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RatingAndReviewUtil.isSuccessFullyReported = true
        btn_close.setOnClickListener {
            activity?.onBackPressed()
        }
        btn_got_it.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}
