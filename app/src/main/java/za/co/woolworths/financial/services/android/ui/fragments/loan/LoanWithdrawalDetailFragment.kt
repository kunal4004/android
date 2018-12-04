package za.co.woolworths.financial.services.android.ui.fragments.loan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.loan_withdrawal_confirmation.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class LoanWithdrawalDetailFragment : LoanBaseFragment() {

    companion object {
        const val DRAWN_DOWN_AMOUNT = "DRAWN_DOWN_AMOUNT"
        fun newInstance(accountInfo: String?) = LoanWithdrawalDetailFragment().withArgs {
            putString(DRAWN_DOWN_AMOUNT, accountInfo)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.loan_withdrawal_confirmation, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}