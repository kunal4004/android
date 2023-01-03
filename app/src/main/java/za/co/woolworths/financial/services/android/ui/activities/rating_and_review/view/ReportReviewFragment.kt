package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentReportReviewBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.ReviewFeedback
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper.RatingAndReviewApiHelper
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ReportReviewsAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModel
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModelFactory
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities

class ReportReviewFragment : Fragment(R.layout.fragment_report_review), ReportReviewsAdapter.ReportItemClick {

    private lateinit var binding: FragmentReportReviewBinding
    private lateinit var moreReviewViewModel: RatingAndReviewViewModel
    private var selectedFeedbacks = mutableListOf<String>()
    private var apiCallInProgress = false

    companion object {
        fun newInstance() = ReportReviewFragment()
        private const val OTHERS = "Others"
        private const val REVIEW = "review"
        private const val INAPPROPRIATE = "inappropriate"
    }

    private lateinit var reportReviewsAdapter: ReportReviewsAdapter
    var reportSuccessFragment: ReportSuccessFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReportReviewBinding.bind(view)

        binding.toolbar.root.setOnClickListener {
            activity?.onBackPressed()
        }
        init()
    }

    fun init() {
        arguments?.apply {
            val reportReviews = getStringArrayList(KotlinUtils.REVIEW_REPORT)
            val review = getSerializable(KotlinUtils.REVIEW_DATA) as Reviews
            reportReviews?.let {
                setDefaultUi(review, reportReviews)
            }
        }
        setupViewModel()
    }

    private fun setupViewModel() {
        moreReviewViewModel = ViewModelProvider(
            this,
            RatingAndReviewViewModelFactory(RatingAndReviewApiHelper())
        ).get(RatingAndReviewViewModel::class.java)
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        apiCallInProgress = true
        /*activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)*/
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        apiCallInProgress = false
        /*activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)*/
    }

    private fun setDefaultUi(review: Reviews, reportReviewList: List<String>) {
        val llm = LinearLayoutManager(requireContext())
        llm.orientation = LinearLayoutManager.VERTICAL
        binding.recylerReport.setLayoutManager(llm)
        reportReviewsAdapter = ReportReviewsAdapter(reportReviewList, this)
        binding.recylerReport.setAdapter(reportReviewsAdapter)
        binding.btnSubmitReport.setOnClickListener {
            if (!apiCallInProgress) {
                showProgressBar()
                lifecycleScope.launch {
                    try {
                        var feedbackText = ""
                        for (feedback in selectedFeedbacks) {
                            feedbackText = if (feedback != OTHERS) {
                                if (feedbackText == "")
                                    feedback
                                else
                                    "$feedbackText|$feedback"
                            } else
                                "$feedbackText|${binding.edtTxtFeedback.text}"
                        }
                        val response = moreReviewViewModel.reviewFeedback(
                            ReviewFeedback(
                                review.id.toString(),
                                SessionUtilities.getInstance().jwt.AtgId.asString,
                                REVIEW,
                                INAPPROPRIATE,
                                null,
                                feedbackText
                            )
                        )
                        if (response.httpCode == 200) {
                        openReportScreenFragment()
                        RatingAndReviewUtil.reportedReviews.add(review.id.toString())
                        hideProgressBar()
                        }
                    } catch (e: HttpException) {
                        e.printStackTrace()
                        hideProgressBar()
                        if(e.code() == 502){
                            binding.tvDuplicateReport.visibility = View.VISIBLE
                        }
                       // RatingAndReviewUtil.reportedReviews.add(review.id.toString())

                    }
                    reportReviewsAdapter.getAllCheckBoxCount()
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
        if (isChecked)
            selectedFeedbacks.add(reportItem)
        else
            selectedFeedbacks.remove(reportItem)

        binding.apply {
            if (reportItem.equals(OTHERS) && isChecked) {
                edtTxtFeedback.visibility = View.VISIBLE
                reviewWriteReportLabel.visibility = View.VISIBLE
            } else if (reportItem.equals(OTHERS) && !isChecked) {
                edtTxtFeedback.visibility = View.INVISIBLE
                reviewWriteReportLabel.visibility = View.INVISIBLE
            }

            if (reportReviewsAdapter.getAllCheckBoxCount() != 0) {
                btnSubmitReport.setBackgroundColor(resources.getColor(R.color.black))
                btnSubmitReport.isEnabled = true
            } else {
                btnSubmitReport.setBackgroundColor(resources.getColor(R.color.gray))
                btnSubmitReport.isEnabled = false
            }
        }
    }

}
