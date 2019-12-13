package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import android.content.IntentFilter
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.IOTPReceiveListener
import za.co.woolworths.financial.services.android.contracts.IStoreCardListener
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.extension.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.npc.EnterOtpFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProcessBlockCardFragment
import za.co.woolworths.financial.services.android.util.SMSReceiver


open class MyCardActivityExtension : AppCompatActivity(), IOTPReceiveListener, IStoreCardListener {

    var mStoreCardDetail: String? = null
    private var otpType: OTPMethodType = OTPMethodType.SMS
    private var cardNumber: String? = null
    private var oTPNumber: String? = null
    var mOtpSentTo: String? = null
    private var mSmsReceiver: SMSReceiver? = null
    var mPhoneNumberOTP: String? = null

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

    fun clearFlag() {
        window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    fun navigateToMyCardActivity(storeCard: String?, cardIsBlocked: Boolean) {
        val openCardDetailActivity = Intent(this, MyCardDetailActivity::class.java)
        openCardDetailActivity.putExtra(ProcessBlockCardFragment.CARD_BLOCKED, cardIsBlocked)
        openCardDetailActivity.putExtra(MyCardDetailActivity.STORE_CARD_DETAIL, storeCard)
        startActivity(openCardDetailActivity)
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    fun hideBackIcon() = supportActionBar?.apply { setDisplayHomeAsUpEnabled(false) }

    fun showBackIcon() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun getStoreCardDetail(): StoreCardsResponse = Gson().fromJson(mStoreCardDetail, StoreCardsResponse::class.java)

    fun startSMSListener() {
        try {
            mSmsReceiver = SMSReceiver()
            mSmsReceiver?.apply {
                setOTPListener(this@MyCardActivityExtension)

                val intentFilter = IntentFilter()
                intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
                this@MyCardActivityExtension.registerReceiver(mSmsReceiver, intentFilter)

                val client = SmsRetriever.getClient(this@MyCardActivityExtension)

                val task = client.startSmsRetriever()
                task.addOnSuccessListener {
                    // API successfully started
                }

                task.addOnFailureListener {
                    // Fail to start API
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelSMSRetriever() = mSmsReceiver?.let { smsReceiver -> unregisterReceiver(smsReceiver) }

    override fun onOTPReceived(otp: String) {
        runOnUiThread {
            val fragment = supportFragmentManager.findFragmentById(R.id.flMyCard)
            (fragment as? EnterOtpFragment)?.onOTPReceived(otp)
        }
    }

    override fun onOTPTimeOut() {}

    override fun onOTPReceivedError(error: String) {}

    override fun navigateToPreviousFragment(errorDescription: String?) {
        super.navigateToPreviousFragment(errorDescription)
        replaceFragmentSafely(
                fragment = EnterOtpFragment.newInstance(mStoreCardDetail, mOtpSentTo),
                tag = EnterOtpFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard,
                allowBackStack = false)
    }
}