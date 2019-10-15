package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.my_card_activity.*
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.InstantStoreCardFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardDetailFragment
import za.co.woolworths.financial.services.android.util.Utils

class InstantStoreCardReplacementActivity : MyCardActivityExtension() {

    private var mStoreCardDetail: String? = null
    private var mCard: String? = null
    private var otpType: OTPMethodType = OTPMethodType.SMS
    private var cardNumber: String? = null
    private var oTPNumber: String? = null
    private var sequenceNumber: String? = null
    var mDefaultOtpSentTo: String? = null // required to save default phone number


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.block_my_card_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        intent?.extras?.apply {
            mCard = getString(MyCardDetailFragment.CARD)
            mStoreCardDetail = getString(MyCardDetailActivity.STORE_CARD_DETAIL)
        }

        if (savedInstanceState == null) {
            addFragment(
                    fragment = InstantStoreCardFragment.newInstance(),
                    tag = InstantStoreCardFragment::class.java.simpleName,
                    containerViewId = R.id.flMyCard)
        }
    }

    private fun actionBar() {
        toolbarText?.text = ""
        setSupportActionBar(tbMyCard)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    override fun onBackPressed() {
        navigateBack()
    }

    private fun navigateBack() {
        with(supportFragmentManager) {
            if (backStackEntryCount > 0) {
                fragments[backStackEntryCount - 1]?.onResume()
                popBackStack()

            } else {
                finishActivity()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> finishActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun finishActivity() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        navigateToMyCardActivity(mStoreCardDetail, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val instanceFragment = supportFragmentManager.findFragmentById(R.id.flMyCard)
        instanceFragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        supportFragmentManager.findFragmentById(R.id.flMyCard)?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun getStoreCardDetail(): Account = Gson().fromJson(mStoreCardDetail, Account::class.java)

    fun getOTPMethodType(): OTPMethodType = this.otpType

    fun setOTPType(otpMethodType: OTPMethodType) {
        this.otpType = otpMethodType
    }

    fun setCardNumber(number: String) {
        this.cardNumber = number
    }

    fun getCardNumber() = this.cardNumber

    fun setOTPNumber(otp: String) {
        oTPNumber = otp
    }

    fun getOtpNumber(): String = oTPNumber ?: ""

    fun setSequenceNumber(sequenceNumber: String) {
        this.sequenceNumber = sequenceNumber
    }

    fun getSequenceNumber() = sequenceNumber?.toInt() ?: 0

    fun clearFlag() {
        window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

}