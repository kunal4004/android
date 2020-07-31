package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IPaymentOptionContract
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.PayMyCardHeaderItem

class PayMyAccountModelImpl : IPaymentOptionContract.PayMyAccountModel {

    override fun getAccountDetailValues(): HashMap<String, String?> {
        return hashMapOf("AccountHolder" to getString(R.string.account_details_account_holder), "AccountNumber" to getString(R.string.account_details_account_number), "Bank" to getString(R.string.account_details_bank), "BranchCode" to getString(R.string.account_details_branch_code), "ReferenceNumber" to getString(R.string.account_details_reference_number), "SwiftCode" to getString(R.string.account_details_swift_code))
    }

    override fun getDrawableHeader(): List<PayMyCardHeaderItem> {
        return listOf(PayMyCardHeaderItem(R.string.store_card_payment_options_title, R.string.store_card_payment_options_desc, R.drawable.w_store_card),
                PayMyCardHeaderItem(R.string.credit_card_payment_options_title, R.string.credit_card_payment_options_desc, R.drawable.w_black_credit_card),
                PayMyCardHeaderItem(R.string.credit_card_payment_options_title, R.string.credit_card_payment_options_desc, R.drawable.w_gold_credit_card),
                PayMyCardHeaderItem(R.string.credit_card_payment_options_title, R.string.credit_card_payment_options_desc, R.drawable.w_silver_credit_card),
                PayMyCardHeaderItem(R.string.personal_loan_payment_options_title, R.string.personal_loan_payment_options_desc, R.drawable.w_personal_loan_card))
    }

    override fun getATMPaymentInfo(): MutableList<Int> {
        return mutableListOf(R.string.atmPaymentInfo1,R.string.atmPaymentInfo2,R.string.atmPaymentInfo3,R.string.atmPaymentInfo4,R.string.atmPaymentInfo5,R.string.atmPaymentInfo6,R.string.atmPaymentInfo7,R.string.atmPaymentInfo8)
    }

    private fun getString(stringId: Int): String? = (WoolworthsApplication.getAppContext().resources?.getString(stringId))
}