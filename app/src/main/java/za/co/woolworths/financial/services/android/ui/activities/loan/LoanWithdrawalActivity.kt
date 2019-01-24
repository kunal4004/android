package za.co.woolworths.financial.services.android.ui.activities.loan

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.loan_withdrawal_layout.*
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.fragments.loan.LoanWithdrawalDetailFragment
import za.co.woolworths.financial.services.android.ui.fragments.loan.LoanWithdrawalFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment.DialogListener
import za.co.woolworths.financial.services.android.util.NetworkChangeListener
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils

class LoanWithdrawalActivity : AppCompatActivity(), DialogListener, NetworkChangeListener {

    private var mConnectionBroadCast: BroadcastReceiver? = null
    private var accountInfo: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this@LoanWithdrawalActivity, R.color.purple)
        setContentView(R.layout.loan_withdrawal_container)
        setActionBar()

        val bundle = intent.extras
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

        connectionDetector()
    }

    private fun connectionDetector() {
        mConnectionBroadCast = Utils.connectionBroadCast(this, this)
    }

    private fun setActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeIndicatorIcon(R.drawable.close_white)
        }
    }

    fun setHomeIndicatorIcon(drawableId: Int) {
        supportActionBar?.setHomeAsUpIndicator(drawableId)
    }

    fun finishActivity() {
        supportFragmentManager?.let {
            (it.backStackEntryCount).let { num ->
                when (num) {
                    1 -> {
                        it.popBackStack()
                    }
                    else -> {
                        finish()
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mConnectionBroadCast, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
        setHomeIndicatorIcon(R.drawable.close_white)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mConnectionBroadCast)
    }

    override fun onBackPressed() {
        finishActivity()
    }

    override fun cancel() {
        (findFragmentByTag(LoanWithdrawalFragment::class.java.simpleName) as LoanWithdrawalFragment?)?.onResume()
    }

    override fun onConnectionChanged() {
        val fragment = supportFragmentManager?.findFragmentById(R.id.flLoanContent)
        val isConnected = NetworkManager.getInstance().isConnectedToNetwork(this@LoanWithdrawalActivity)
        when (fragment) {
            is LoanWithdrawalFragment -> fragment.onConnectionChanged(isConnected)
            is LoanWithdrawalDetailFragment -> fragment.onConnectionChanged(isConnected)
        }
    }
}