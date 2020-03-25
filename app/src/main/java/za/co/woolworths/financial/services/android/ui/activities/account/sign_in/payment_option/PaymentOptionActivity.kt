package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.payment_option

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.payment_options_activity.*
import kotlinx.android.synthetic.main.payment_options_header.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IPaymentOptionContract
import za.co.woolworths.financial.services.android.models.dto.PaymentMethod
import za.co.woolworths.financial.services.android.models.dto.account.PaymentOptionHeaderItem
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.KotlinUtils

class PaymentOptionActivity : AppCompatActivity(), View.OnClickListener, IPaymentOptionContract.PaymentOptionView {

    private var mPaymentOptionPresenterImpl: PaymentOptionPresenterImpl? = null

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
        mPaymentOptionPresenterImpl = PaymentOptionPresenterImpl(this, PaymentOptionModelImpl())
        mPaymentOptionPresenterImpl?.apply {
            retrieveAccountBundle(intent)
            initView()
        }
        closeButtonImageView?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeButtonImageView -> onBackPressed()
        }
    }

    override fun showPaymentDetail(paymentDetail: Map<String, String>?) {
        howToPayAccountDetails?.removeAllViews()
        paymentDetail?.forEach { paymentItem ->
            val view = View.inflate(this, R.layout.how_to_pay_account_details_list_item, null)
            val paymentName: WTextView? = view?.findViewById(R.id.paymentName)
            val paymentValue: WTextView? = view?.findViewById(R.id.paymentvalue)
            paymentName?.text = paymentItem.key
            paymentValue?.text = paymentItem.value
            howToPayAccountDetails?.addView(view)
        }
    }

    override fun setHowToPayLogo(paymentOptionHeaderItem: PaymentOptionHeaderItem?) {
        paymentOptionHeaderItem?.apply {
            creditCardPaymentOptionTextView?.text = getString(title)
            payWooliesCardTextView?.text = getString(description)
            cardOptionImageView?.setImageResource(card)
            viewBackground?.setBackgroundResource(background)
        }
    }

    override fun showABSAInfo() {
        llAbsaAccount?.visibility = VISIBLE
        llCreditCardDetail?.visibility = VISIBLE
        tvHowToPayTitle?.text = getString(R.string.how_to_pay_credit_card_title)
    }

    override fun hideABSAInfo() {
        llAbsaAccount?.visibility = GONE
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

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }
}
