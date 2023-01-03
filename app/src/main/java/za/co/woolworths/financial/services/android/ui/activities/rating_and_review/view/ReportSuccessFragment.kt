package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.view.View
import com.awfs.coordination.databinding.FragmentReportReviewSuccessBinding
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ReportSuccessFragment : BaseFragmentBinding<FragmentReportReviewSuccessBinding>(FragmentReportReviewSuccessBinding::inflate) {

    companion object {
        fun newInstance() = ReportSuccessFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RatingAndReviewUtil.isSuccessFullyReported = true
        binding.btnClose.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.btnGotIt.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}
