package za.co.woolworths.financial.services.android.ui.activities.loan

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.LoanWithdrawalContainerBinding
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.fragments.loan.LoanWithdrawalDetailFragment
import za.co.woolworths.financial.services.android.ui.fragments.loan.LoanWithdrawalFragment
import za.co.woolworths.financial.services.android.util.NetworkChangeListener
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils

class LoanWithdrawalActivity : AppCompatActivity(), IDialogListener, NetworkChangeListener {

    private lateinit var binding: LoanWithdrawalContainerBinding
    private var mConnectionBroadCast: BroadcastReceiver? = null
    private var accountInfo: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this@LoanWithdrawalActivity, R.color.purple)
        binding = LoanWithdrawalContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()

        intent?.extras?.apply {
            accountInfo = getString("account_info")
        }

        if (savedInstanceState == null) {
            addFragment(
                    fragment = LoanWithdrawalFragment.newInstance(accountInfo),
                    tag = LoanWithdrawalFragment::class.java.simpleName,
                    containerViewId = R.id.flLoanContent
            )
        }

        connectionDetector()

        uniqueIdsForPLDDModule()
    }

    private fun uniqueIdsForPLDDModule() {
        binding.toolbar?.contentDescription = getString(R.string.PLDD_toolbar_layout)
    }

    private fun connectionDetector() {
        mConnectionBroadCast = Utils.connectionBroadCast(this, this)
    }

    private fun setActionBar() {
        setSupportActionBar(binding.toolbar)
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
        supportFragmentManager.let {
            it.backStackEntryCount.let { count ->
                when (count) {
                    1 -> it.popBackStack()
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

    override fun onDialogDismissed() {
        (findFragmentByTag(LoanWithdrawalFragment::class.java.simpleName) as? LoanWithdrawalFragment)?.onResume()
    }

    override fun onConnectionChanged() {
        val isConnected = NetworkManager.getInstance().isConnectedToNetwork(this@LoanWithdrawalActivity)
       supportFragmentManager.findFragmentById(R.id.flLoanContent)?.apply {
           when (this) {
               is LoanWithdrawalFragment -> onConnectionChanged(isConnected)
               is LoanWithdrawalDetailFragment -> onConnectionChanged(isConnected)
           }
       }
    }

    override fun onDialogButtonAction() {
    }

    fun setContentDescriptionForActionBarHomeIcon(name : String) {
        supportActionBar?.setHomeActionContentDescription(name)
    }
}