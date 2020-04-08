package za.co.woolworths.financial.services.android.ui.fragments.loan

import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.loan_confirmation_layout.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.IssueLoan
import za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanRequest
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.util.DialogManager
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.SessionUtilities

class LoanWithdrawalDetailFragment : LoanBaseFragment() {

    private var mIssueLoan: IssueLoan? = null
    private var mInstallmentAmount = 0
    private var mAuthoriseLoan: Call<AuthoriseLoanResponse>? = null
    private var autoIssueLoanConnectIsActivated: Boolean = false
    private var mErrorHandlerView: ErrorHandlerView? = null

    companion object {
        const val ISSUE_LOAN = "ISSUE_LOAN_REQUEST"
        const val INSTALLMENT_AMOUNT = "INSTALLMENT_AMOUNT"
        fun newInstance(accountInfo: String?, installmentAmount: Int) = LoanWithdrawalDetailFragment().withArgs {
            putString(ISSUE_LOAN, accountInfo)
            putInt(INSTALLMENT_AMOUNT, installmentAmount)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            mIssueLoan = getString(ISSUE_LOAN)?.let { Gson().fromJson(it, IssueLoan::class.java) }
            mInstallmentAmount = getInt(INSTALLMENT_AMOUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.loan_confirmation_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            mErrorHandlerView = ErrorHandlerView(it)
            (it as? LoanWithdrawalActivity)?.setHomeIndicatorIcon(R.drawable.back_white)
            mIssueLoan?.apply {
                tvDrawnDownSelectedAmount?.text = currencyFormatter(drawDownAmount, it)
                val repaymentPeriod = "$repaymentPeriod month".plus(if (repaymentPeriod == 1) "" else "s")
                tvRepaymentPeriod?.text = repaymentPeriod
                tvAdditionalMonthlyRepayment?.text = currencyFormatter((mInstallmentAmount), it)
            }
        }

        btnConfirm?.setOnClickListener { authoriseLoanRequest() }
        uniqueIdsForLoanWithdrawalDetails()
    }

    private fun uniqueIdsForLoanWithdrawalDetails() {
        activity?.resources?.let {
            txtWithdrawAmount?.contentDescription = getString(R.string.pldd_drawn_down_amount_description_layout)
            rlRepaymentPeriod?.contentDescription = getString(R.string.repayment_period_layout)
            rlAdditionalMonthlyPayment?.contentDescription = getString(R.string.additional_monthly_payment_layout)
            btnConfirm?.contentDescription = getString(R.string.confirm_repayment_period_layout)
            confirmLoanRequestConstraintLayout?.contentDescription = getString(R.string.confirm_loan_layout)
            tvDrawnDownSelectedAmount?.contentDescription = getString(R.string.drawn_down_amount_label)
        }
    }

    private fun authoriseLoanRequest() {
        progressBarVisibility(true)

        mAuthoriseLoan =  OneAppService.authoriseLoan(AuthoriseLoanRequest(mIssueLoan!!.productOfferingId,
                mIssueLoan!!.drawDownAmount,mIssueLoan!!.repaymentPeriod, mInstallmentAmount,
                mIssueLoan!!.creditLimit))

        mAuthoriseLoan?.enqueue(CompletionHandler(object : IResponseListener<AuthoriseLoanResponse> {
            override fun onSuccess(authoriseLoanResponse: AuthoriseLoanResponse?) {
                activity?.let { fragmentActivity ->
                    progressBarVisibility(false)
                    autoIssueLoanConnectIsActivated = false
                    authoriseLoanResponse?.apply {
                        when (httpCode) {
                            200 -> {
                                replaceFragment(
                                        fragment = LoanWithdrawalSuccessFragment.newInstance(),
                                        tag = LoanWithdrawalSuccessFragment::class.java.simpleName,
                                        containerViewId = R.id.flLoanContent,
                                        allowStateLoss = true,
                                        enterAnimation = R.anim.slide_in_from_right,
                                        exitAnimation = R.anim.slide_to_left,
                                        popEnterAnimation = R.anim.slide_from_left,
                                        popExitAnimation = R.anim.slide_to_right)
                            }
                            440 -> {
                                response.stsParams.let { stsParams ->
                                    SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE,
                                            stsParams, fragmentActivity)
                                }
                            }
                            else ->
                                authoriseLoanResponse.response?.desc?.let { desc -> DialogManager(fragmentActivity).showBasicDialog(desc) }
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                activity?.apply {
                    progressBarVisibility(false)
                    autoIssueLoanConnectIsActivated = true
                }
            }
        },AuthoriseLoanResponse::class.java))
    }

    private fun progressBarVisibility(visible: Boolean) {
        mConfirmProgressBar?.visibility = if (visible) VISIBLE else GONE
        btnConfirm?.visibility = if (visible) GONE else VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onBackPressed() {
        activity?.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAuthoriseLoan?.let {
            if (!it.isCanceled)
                it.cancel()
        }
    }

    fun onConnectionChanged(hasInternet: Boolean) {
        activity?.runOnUiThread {
            if (hasInternet) {
                if (arrowIsVisible && autoIssueLoanConnectIsActivated) {
                    progressBarVisibility(true)
                    authoriseLoanRequest()
                }
            } else {
                progressBarVisibility(false)
                mErrorHandlerView?.showToast()
            }
        }
    }
}