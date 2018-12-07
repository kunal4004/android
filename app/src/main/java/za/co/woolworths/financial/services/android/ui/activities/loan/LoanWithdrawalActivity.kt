package za.co.woolworths.financial.services.android.ui.activities.loan

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.loan_withdrawal_layout.*
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.loan.LoanWithdrawalFragment
import za.co.woolworths.financial.services.android.util.Utils

class LoanWithdrawalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this@LoanWithdrawalActivity, R.color.purple)
        setContentView(R.layout.loan_withdrawal_container)
        setActionBar()

        val bundle = intent.extras
        var accountInfo: String? = ""
        if (bundle != null) {
            accountInfo = bundle.getString("account_info")
        }

        if (savedInstanceState == null) {
            addFragment(
                    fragment = LoanWithdrawalFragment.newInstance(accountInfo),
                    tag = LoanWithdrawalFragment::class.java.simpleName,
                    containerViewId = R.id.flLoanContent
            )
        }
    }

    private fun setActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowTitleEnabled(false)
            it.setDisplayUseLogoEnabled(false)
            it.setHomeAsUpIndicator(R.drawable.close_white)
        }
    }

    fun finishActivity() {
        supportFragmentManager?.let {
            if (it.backStackEntryCount == 1) {
                it.popBackStack()
            } else {
                finish()
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }
    }

    override fun onBackPressed() {
        finishActivity()
    }
}