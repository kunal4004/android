package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.payment_option

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.payment_options_activity.*
import kotlinx.android.synthetic.main.payment_options_activity.whatsAppIconImageView
import kotlinx.android.synthetic.main.payment_options_activity.whatsAppNextIconImageView
import kotlinx.android.synthetic.main.payment_options_activity.whatsAppTitleTextView
import kotlinx.android.synthetic.main.payment_options_header.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IPaymentOptionContract
import za.co.woolworths.financial.services.android.models.dto.PaymentMethod
import za.co.woolworths.financial.services.android.models.dto.account.PayMyCardHeaderItem
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountModelImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs.Companion.FEATURE_WHATSAPP
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WhatsAppUnavailableFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.ScreenManager

class PayMyAccountActivity : AppCompatActivity(), View.OnClickListener, IPaymentOptionContract.PayMyAccountView {

    private var mPayMyAccountPresenterImpl: PayMyAccountPresenterImpl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KotlinUtils.setTransparentStatusBar(this)
        setContentView(R.layout.payment_options_activity)
        initViews()
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.HOW_TO_PAY)
    }

    private fun initViews() {
        mPayMyAccountPresenterImpl = PayMyAccountPresenterImpl(this, PayMyAccountModelImpl())
        mPayMyAccountPresenterImpl?.apply {
            retrieveAccountBundle(intent)
            initView()
        }
        closeButtonImageView?.setOnClickListener(this)
        paymentOptionChatToUsRelativeLayout?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeButtonImageView -> onBackPressed()
            R.id.paymentOptionChatToUsRelativeLayout -> {
                if (!WhatsAppChatToUs().isCustomerServiceAvailable) {
                    val whatsAppUnavailableFragment = WhatsAppUnavailableFragment()
                    whatsAppUnavailableFragment.show(supportFragmentManager, WhatsAppUnavailableFragment::class.java.simpleName)
                    return
                }

                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WHATSAPP_PAYMENT_OPTION)
                ScreenManager.presentWhatsAppChatToUsActivity(this@PayMyAccountActivity, FEATURE_WHATSAPP, mPayMyAccountPresenterImpl?.getAppScreenName())
            }
        }
    }

    override fun setPaymentOption(paymentMethods: MutableList<PaymentMethod>?) {
        howToPayOptionsList?.removeAllViews()
        paymentMethods?.forEachIndexed { index, paymentMethod ->
            val view = View.inflate(this, R.layout.how_to_pay_options_list_item, null)
            val count: WTextView? = view.findViewById(R.id.count)
            val howToPayOption: WTextView? = view.findViewById(R.id.howToPayOption)
            count?.text = (index + 1).toString()
            howToPayOption?.text = paymentMethod.description
            howToPayOptionsList?.addView(view)
        }
    }

    override fun setWhatsAppChatWithUsVisibility(isVisible: Boolean) {
        if (isVisible) {
            chatWithUsContainerLinearLayout?.visibility = VISIBLE
            // Customer service availability
            if (WhatsAppChatToUs().isCustomerServiceAvailable) {
                whatsAppIconImageView?.setImageResource(R.drawable.icon_whatsapp_green)
                whatsAppTitleTextView?.setTextColor(Color.BLACK)
                whatsAppNextIconImageView?.alpha = 1f
            } else {
                whatsAppIconImageView?.setImageResource(R.drawable.icon_whatsapp_grey)
                whatsAppNextIconImageView?.alpha = 0.4f
                whatsAppTitleTextView?.setTextColor(ContextCompat.getColor(this@PayMyAccountActivity, R.color.unavailable))
            }
        } else {
            chatWithUsContainerLinearLayout?.visibility = GONE
        }
    }

    override fun getPayMyAccountPresenter(): PayMyAccountPresenterImpl? {
        return mPayMyAccountPresenterImpl
    }

    override fun showPaymentDetail(paymentDetail: Map<String, String>?) {
        howToPayAccountDetails?.removeAllViews()
        paymentDetail?.forEach { paymentItem ->
            val view = View.inflate(this, R.layout.atm_banking_detail_item, null)
            val paymentName: TextView? = view?.findViewById(R.id.paymentName)
            val paymentValue: TextView? = view?.findViewById(R.id.paymentvalue)
            val accountLabel = KotlinUtils.capitaliseFirstLetter(KotlinUtils.addSpaceBeforeUppercase(paymentItem.key) + ":")
            paymentName?.text = accountLabel
            paymentValue?.text = paymentItem.value
            howToPayAccountDetails?.addView(view)
        }
    }

    override fun setHowToPayLogo(payMyCardHeaderItem: PayMyCardHeaderItem?) {
        payMyCardHeaderItem?.apply {
            creditCardPaymentOptionTextView?.text = getString(title)
            payWooliesCardTextView?.text = getString(description)
            cardOptionImageView?.setImageResource(card)
        }
    }

    override fun showABSAInfo() {
        tvHowToPayTitle?.text = getString(R.string.payment_made_from_other_acc_title)
        llAbsaAccount?.visibility = VISIBLE
        tvPaymentOtherAccountDesc?.visibility = VISIBLE
    }

    override fun hideABSAInfo() {
        llAbsaAccount?.visibility = GONE
        tvPaymentOtherAccountDesc?.visibility = GONE

    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }
}
