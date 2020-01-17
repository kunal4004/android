package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.payment_option

import androidx.fragment.app.FragmentActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.PaymentOptionContract
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.HeaderDrawable

class PaymentOptionModelImpl : PaymentOptionContract.PaymentOptionModel {

    override fun getAccountDetailValues(): HashMap<String, String?> {
        return hashMapOf("accountHolder" to getString(R.string.account_details_account_holder), "accountNumber" to getString(R.string.account_details_account_number), "bank" to getString(R.string.account_details_bank), "branchCode" to getString(R.string.account_details_branch_code), "referenceNumber" to getString(R.string.account_details_reference_number), "swiftCode" to getString(R.string.account_details_swift_code))
    }

    override fun getDrawableHeader(): List<HeaderDrawable> {
        return listOf(HeaderDrawable(R.drawable.w_store_card, R.drawable.store_card_background),
                HeaderDrawable(R.drawable.w_black_credit_card, R.drawable.black_credit_card_background),
                HeaderDrawable(R.drawable.w_gold_credit_card, R.drawable.gold_credit_card_background),
                HeaderDrawable(R.drawable.w_silver_credit_card, R.drawable.store_card_background),
                HeaderDrawable(R.drawable.w_personal_loan_card, R.drawable.personal_loan_background))
    }

    private fun getString(stringId: Int): String? = (WoolworthsApplication.getInstance()?.currentActivity as? FragmentActivity)?.resources?.getString(stringId) ?: ""
}