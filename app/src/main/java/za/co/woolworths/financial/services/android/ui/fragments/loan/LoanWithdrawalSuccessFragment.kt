package za.co.woolworths.financial.services.android.ui.fragments.loan

import android.os.Bundle
import android.util.Log
import android.view.*
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.loan_withdrawal_success.*
import za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity

class LoanWithdrawalSuccessFragment : LoanBaseFragment() {

    companion object {
        fun newInstance() = LoanWithdrawalSuccessFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.loan_withdrawal_success, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { (it as LoanWithdrawalActivity).setHomeIndicatorIcon(R.drawable.close_white) }

        btnOk.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onBackPressed() {
        finishActivity(activity)
    }

    fun onConnectionChanged(hasInternet: Boolean) {
        Log.e("hasInternrt:", "hasInternet $hasInternet")

    }
}