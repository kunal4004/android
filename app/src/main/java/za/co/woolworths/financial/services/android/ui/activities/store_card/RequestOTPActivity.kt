package za.co.woolworths.financial.services.android.ui.activities.store_card

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.RequestOTPFragment
import za.co.woolworths.financial.services.android.util.Utils

class RequestOTPActivity : AppCompatActivity() {

    companion object {
        const val OTP_SENT_TO = "OTP_SENT_TO"
        const val OTP_VALUE = "OTP_VALUE"
        const val OTP_REQUEST_CODE = 1983
    }

    private var mOtpSentTo: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_otp)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            mOtpSentTo = getString(OTP_SENT_TO, "")
        }
        addRequestOTPFragment()
    }


    private fun addRequestOTPFragment() {
        mOtpSentTo?.apply {
            addFragment(
                    fragment = RequestOTPFragment.newInstance(this),
                    tag = RequestOTPFragment::class.java.simpleName,
                    containerViewId = R.id.fragmentContainer)
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

    private fun finishActivity() {
        this.finish()
        this.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }


}