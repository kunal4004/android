package za.co.woolworths.financial.services.android.ui.fragments.loan

import android.os.Bundle
import android.util.Log
import android.view.*
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.loan_withdrawal_confirmation.*
import za.co.woolworths.financial.services.android.models.dto.IssueLoan
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class LoanWithdrawalDetailFragment : LoanBaseFragment() {

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

        var issueLoan: IssueLoan? = null
        var installmentAmount = 0

        arguments?.let { bundle ->
            issueLoan = bundle.getString(ISSUE_LOAN)?.let { Gson().fromJson(it, IssueLoan::class.java) }!!
            installmentAmount = bundle.getInt(INSTALLMENT_AMOUNT)
        }

        val repaymentPeriod = issueLoan?.repaymentPeriod

        activity?.let {
            tvDrawnDownSelectedAmount.text = currencyFormatter((issueLoan?.drawDownAmount!!), it)
            tvRepaymentPeriod.text = repaymentPeriod?.toString()?.plus(" month".plus(if (repaymentPeriod == 1) "" else "s"))
            tvAdditionalMonthlyRepayment.text = currencyFormatter((installmentAmount), it)
        }

        btnConfirm.setOnClickListener { Log.e("blink", "blimp") }

        // btnConfirm
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.loan_withdrawal_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        //  this.mMenu = menu
        //  menuItemVisible(menu, false)
        super.onPrepareOptionsMenu(menu)
    }
}