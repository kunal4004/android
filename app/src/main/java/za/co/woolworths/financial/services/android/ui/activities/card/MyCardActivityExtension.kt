package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.fragments.npc.ProcessBlockCardFragment

open class MyCardActivityExtension : AppCompatActivity() {

    var mStoreCardDetail: String? = null
    private var otpType: OTPMethodType = OTPMethodType.SMS
    private var cardNumber: String? = null
    private var oTPNumber: String? = null
    var mDefaultOtpSentTo: String? = null // required to save default phone number

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
}