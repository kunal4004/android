package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_report_review.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.ReviewFeedback
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper.RatingAndReviewApiHelper
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view.adapter.ReportReviewsAdapter
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModel
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModelFactory
import za.co.woolworths.financial.services.android.util.FirebaseAnalyticsUserProperty
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class ReportReviewFragment : Fragment(), ReportReviewsAdapter.ReportItemClick {
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
        progressBar.visibility = View.VISIBLE
        apiCallInProgress = true
        /*activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)*/
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
        apiCallInProgress = false
        /*activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)*/
    }

    private fun setDefaultUi(review: Reviews, reportReviewList: List<String>) {
        val llm = LinearLayoutManager(requireContext())
        llm.orientation = LinearLayoutManager.VERTICAL
        recyler_report.setLayoutManager(llm)
        reportReviewsAdapter = ReportReviewsAdapter(reportReviewList, this)
        recyler_report.setAdapter(reportReviewsAdapter)
        btn_submit_report.setOnClickListener {
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
                                "$feedbackText|${edt_txt_feedback.text}"
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
                        review.isReported = true
                        hideProgressBar()
                        }
                    } catch (e: HttpException) {
                        e.printStackTrace()
                        hideProgressBar()
                        openReportScreenFragment()
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
        if (reportItem.equals(OTHERS) && isChecked) {
            edt_txt_feedback.visibility = View.VISIBLE
        } else if (reportItem.equals(OTHERS) && !isChecked) {
            edt_txt_feedback.visibility = View.INVISIBLE
        }

        if (reportReviewsAdapter.getAllCheckBoxCount() != 0) {
            btn_submit_report.setBackgroundColor(resources.getColor(R.color.black))
            btn_submit_report.isEnabled = true
        } else {
            btn_submit_report.setBackgroundColor(resources.getColor(R.color.gray))
            btn_submit_report.isEnabled = false
        }
    }

}
