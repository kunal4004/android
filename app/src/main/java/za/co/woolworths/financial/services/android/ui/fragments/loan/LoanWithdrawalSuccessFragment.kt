package za.co.woolworths.financial.services.android.ui.fragments.loan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.LoanWithdrawalSuccessBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.loan.LoanWithdrawalActivity
import za.co.woolworths.financial.services.android.util.Utils

class LoanWithdrawalSuccessFragment : LoanBaseFragment() {

    companion object {
        fun newInstance() = LoanWithdrawalSuccessFragment()
    }

    private lateinit var binding: LoanWithdrawalSuccessBinding

    override fun onStart() {
        super.onStart()
        activity?.apply {Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.personalLoanDrawdownComplete, this) }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LoanWithdrawalSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { (it as? LoanWithdrawalActivity)?.setHomeIndicatorIcon(R.drawable.close_white) }

        binding.btnOk?.setOnClickListener {
            onBackPressed()
        }

        binding.uniqueIdsForPLDDModule()
    }

    private fun LoanWithdrawalSuccessBinding.uniqueIdsForPLDDModule() {
        linLoanWithdrawalSuccess?.contentDescription = getString(R.string.loan_withdrawal_success_layout)
        btnOk?.contentDescription = getString(R.string.on_success_button_tapped)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onBackPressed() {
        activity?.let { finishActivity(it) }
    }
}