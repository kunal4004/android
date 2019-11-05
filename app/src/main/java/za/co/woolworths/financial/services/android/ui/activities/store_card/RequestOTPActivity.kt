package za.co.woolworths.financial.services.android.ui.activities.store_card

import android.os.Bundle
import android.view.MenuItem
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.EnterOtpFragment
import za.co.woolworths.financial.services.android.util.Utils

class RequestOTPActivity : MyCardActivityExtension() {

    companion object {
        const val OTP_SENT_TO = "OTP_SENT_TO"
        const val OTP_VALUE = "OTP_VALUE"
        const val OTP_REQUEST_CODE = 1983
    }
    var otpSentTo: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_otp)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            otpSentTo = getString(OTP_SENT_TO, "")
        }
        addRequestOTPFragment()
    }


    private fun addRequestOTPFragment() {
        Bundle().let { bundle ->
            bundle.putBoolean(EnterOtpFragment.IS_UNBLOCK_VIRTUAL_CARD, true)
            EnterOtpFragment.newInstance().let {
                it.arguments = bundle
                addFragment(
                        fragment = it,
                        tag = EnterOtpFragment::class.java.simpleName,
                        containerViewId = R.id.fragmentContainer)
            }

        }
    }

    private fun actionBar() {
        setSupportActionBar(tbMyCard)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateBack()
                return true
            }
            else -> false
        }
    }

    override fun onBackPressed() = navigateBack()


    private fun navigateBack() {
        supportFragmentManager?.apply {
            if (backStackEntryCount > 0) {
                popBackStack()
            } else
                finishActivity()
        }
    }

    fun finishActivity() {
        this.finish()
        this.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }


}