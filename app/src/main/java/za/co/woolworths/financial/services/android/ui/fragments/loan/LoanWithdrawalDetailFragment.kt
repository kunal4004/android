package za.co.woolworths.financial.services.android.ui.fragments.loan

import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.loan_withdrawal_confirmation.*
import za.co.woolworths.financial.services.android.models.dto.IssueLoan
import za.co.woolworths.financial.services.android.models.rest.loan.AuthoriseLoan
import za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanRequest
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanResponse
import za.co.woolworths.financial.services.android.util.DialogManager
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.OnEventListener
import za.co.woolworths.financial.services.android.util.SessionUtilities

class LoanWithdrawalDetailFragment : LoanBaseFragment() {

    private var mIssueLoan: IssueLoan? = null
    private var mInstallmentAmount = 0
    private var mRepaymentPeriod: Int? = 0
    private var mDrawnDownAmount: Int? = 0
    private var mAuthoriseLoan: AuthoriseLoan? = null
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
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.loan_confirmation_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            mErrorHandlerView = ErrorHandlerView(it)
            (it as LoanWithdrawalActivity).setHomeIndicatorIcon(R.drawable.back_white)
        }

        arguments?.let { bundle ->
            mIssueLoan = bundle.getString(ISSUE_LOAN)?.let { Gson().fromJson(it, IssueLoan::class.java) }!!
            mInstallmentAmount = bundle.getInt(INSTALLMENT_AMOUNT)
        }

        mRepaymentPeriod = mIssueLoan?.repaymentPeriod
        mDrawnDownAmount = mIssueLoan?.drawDownAmount!!

        activity?.let {
            tvDrawnDownSelectedAmount.text = currencyFormatter(mDrawnDownAmount!!, it)
            tvRepaymentPeriod.text = mRepaymentPeriod?.toString()?.plus(" month".plus(if (mRepaymentPeriod == 1) "" else "s"))
            tvAdditionalMonthlyRepayment.text = currencyFormatter((mInstallmentAmount), it)
        }

        btnConfirm.setOnClickListener { authoriseLoanRequest() }
    }


    private fun authoriseLoanRequest() {
        progressBarVisibility(true)
        mAuthoriseLoan = AuthoriseLoan(AuthoriseLoanRequest(mIssueLoan!!.productOfferingId,
                mIssueLoan!!.drawDownAmount, mIssueLoan!!.repaymentPeriod, mInstallmentAmount,
                mIssueLoan!!.creditLimit), object : OnEventListener<AuthoriseLoanResponse> {

            override fun onSuccess(`object`: AuthoriseLoanResponse?) {
                progressBarVisibility(false)
                val authoriseLoanResponse: AuthoriseLoanResponse = `object` as AuthoriseLoanResponse
                autoIssueLoanConnectIsActivated = false
                when (authoriseLoanResponse.httpCode) {
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
                        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE,
                                authoriseLoanResponse.response.stsParams, activity)
                    }
                    else ->
                        authoriseLoanResponse.response?.let { result -> DialogManager(activity).showBasicDialog(result.desc) }
                }
            }
            override fun onFailure(e: String?) {
                progressBarVisibility(false)
                autoIssueLoanConnectIsActivated = true
            }
        })

        mAuthoriseLoan!!.execute()
    }

    private fun progressBarVisibility(visible: Boolean) {
        mConfirmProgressBar.visibility = if (visible) VISIBLE else GONE
        btnConfirm.visibility = if (visible) GONE else VISIBLE
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
            if (!it.isCancelled)
                it.cancel(true)
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