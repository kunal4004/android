package za.co.woolworths.financial.services.android.ui.fragments.loan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R

class LoanWithdrawalDetailFragment : LoanBaseFragment() {

    companion object {
        fun newInstance(accountInfo: String?) = LoanWithdrawalDetailFragment().apply {
            arguments = Bundle(1).apply {
                putString("accountInfo", accountInfo)
            }
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