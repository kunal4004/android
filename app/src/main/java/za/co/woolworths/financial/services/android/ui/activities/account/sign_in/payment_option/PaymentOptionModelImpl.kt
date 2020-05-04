package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.payment_option

import androidx.fragment.app.FragmentActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IPaymentOptionContract
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.PaymentOptionHeaderItem

class PaymentOptionModelImpl : IPaymentOptionContract.PaymentOptionModel {

    override fun getAccountDetailValues(): HashMap<String, String?> {
        return hashMapOf("AccountHolder" to getString(R.string.account_details_account_holder), "AccountNumber" to getString(R.string.account_details_account_number), "Bank" to getString(R.string.account_details_bank), "BranchCode" to getString(R.string.account_details_branch_code), "ReferenceNumber" to getString(R.string.account_details_reference_number), "SwiftCode" to getString(R.string.account_details_swift_code))
    }

    override fun getDrawableHeader(): List<PaymentOptionHeaderItem> {
        return listOf(PaymentOptionHeaderItem(R.string.store_card_payment_options_title,R.string.store_card_payment_options_desc,R.drawable.w_store_card, R.drawable.store_card_background),
                PaymentOptionHeaderItem(R.string.credit_card_payment_options_title,R.string.credit_card_payment_options_desc,R.drawable.w_black_credit_card, R.drawable.black_credit_card_background),
                PaymentOptionHeaderItem(R.string.credit_card_payment_options_title,R.string.credit_card_payment_options_desc,R.drawable.w_gold_credit_card, R.drawable.gold_credit_card_background),
                PaymentOptionHeaderItem(R.string.credit_card_payment_options_title,R.string.credit_card_payment_options_desc,R.drawable.w_silver_credit_card, R.drawable.store_card_background),
                PaymentOptionHeaderItem(R.string.personal_loan_payment_options_title,R.string.personal_loan_payment_options_desc,R.drawable.w_personal_loan_card, R.drawable.personal_loan_background))
    }

    private fun getString(stringId: Int): String? = (WoolworthsApplication.getInstance()?.currentActivity as? FragmentActivity)?.resources?.getString(stringId) ?: ""
}