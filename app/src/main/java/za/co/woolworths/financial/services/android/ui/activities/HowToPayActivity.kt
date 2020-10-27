package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.accounts_how_to_pay.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PaymentMethod
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.ui.views.WTextView

class HowToPayActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var accountDetails: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.accounts_how_to_pay)
        Utils.updateStatusBarBackground(this)
        initViews()
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.HOW_TO_PAY)
    }

    private fun initViews() {
        this.accountDetails = Gson().fromJson(intent.getStringExtra("account"), Account::class.java)
        btnClose.setOnClickListener(this)
        setHowToPayLogo()
        loadPaymentOptions()
        loadAccountDetails()
        loadAbsaCreditCardInfoIfNeeded();
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnClose -> {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    private fun loadAccountDetails() {
        val accountDetailValues: HashMap<String, String> = hashMapOf("accountHolder" to getString(R.string.account_details_account_holder), "accountNumber" to getString(R.string.account_details_account_number), "bank" to getString(R.string.account_details_bank), "branchCode" to getString(R.string.account_details_branch_code), "referenceNumber" to getString(R.string.account_details_reference_number), "swiftCode" to getString(R.string.account_details_swift_code))
        howToPayAccountDetails.removeAllViews()
        val inflater: LayoutInflater = LayoutInflater.from(this)
        var paymentDetails: Map<String, String> = Gson().fromJson(accountDetails.bankingDetails, object : TypeToken<Map<String, String>>() {}.type)

        for (i in paymentDetails) {
            val v: View = inflater.inflate(R.layout.how_to_pay_account_details_list_item, howToPayAccountDetails, false)
            var paymentName: WTextView = v.findViewById(R.id.paymentName)
            var paymentValue: WTextView = v.findViewById(R.id.paymentvalue)
            paymentName.text = accountDetailValues[i.key]
            paymentValue.text = i.value
            howToPayAccountDetails.addView(v)
        }
    }

    private fun loadPaymentOptions() {
        var paymentMethods: List<PaymentMethod> = accountDetails.paymentMethods
        howToPayOptionsList.removeAllViews()
        val inflater: LayoutInflater = LayoutInflater.from(this)
        paymentMethods.forEachIndexed { index, paymentMethod ->
            val v: View = inflater.inflate(R.layout.how_to_pay_options_list_item, howToPayOptionsList, false)
            var count: WTextView = v.findViewById(R.id.count)
            var howToPayOption: WTextView = v.findViewById(R.id.howToPayOption)
            count.text = (index + 1).toString()
            howToPayOption.text = paymentMethod.description
            howToPayOptionsList.addView(v)
        }
    }

    private fun setHowToPayLogo() {
        when (AccountsProductGroupCode.getEnum(accountDetails.productGroupCode)) {
            AccountsProductGroupCode.STORE_CARD -> {
                howToPayLogo.setBackgroundResource(R.drawable.how_to_pay_store_card)
                howToPayTitle.text = resources.getString(R.string.ways_to_pay_your_account_cc_sc)
            }
            AccountsProductGroupCode.CREDIT_CARD -> {
                howToPayLogo.setBackgroundResource(R.drawable.how_to_pay_credit_card)
                howToPayTitle.text = resources.getString(R.string.ways_to_pay_your_account_cc_sc)
            }
            AccountsProductGroupCode.PERSONAL_LOAN -> {
                howToPayLogo.setBackgroundResource(R.drawable.how_to_pay_p_loan)
                howToPayTitle.text = resources.getString(R.string.ways_to_pay_your_account_pl)
            }
        }
    }

    private fun loadAbsaCreditCardInfoIfNeeded() {
        when (AccountsProductGroupCode.getEnum(accountDetails.productGroupCode)) {
            AccountsProductGroupCode.CREDIT_CARD -> {
                llAbsaAccount.visibility = VISIBLE
                llCreditCardDetail.visibility = VISIBLE
                tvHowToPayTitle.text = getString(R.string.how_to_pay_credit_card_title)
            }
            else -> {
                llAbsaAccount.visibility = GONE
            }
        }
    }
}
