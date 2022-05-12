package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing

import android.os.Parcelable
import com.awfs.coordination.R
import kotlinx.android.parcel.Parcelize

sealed class InformationData : Parcelable {

    abstract val info: ArrayList<AccountInformation>

    @Parcelize
    data class GoodStanding(
        override val info: ArrayList<AccountInformation> = arrayListOf(
            AccountInformation(R.string.info_available_funds_title, R.string.black_credit_card_available_fund_information_desc),
            AccountInformation(R.string.info_current_balance_title, R.string.black_credit_card_current_balance_information_desc),
            AccountInformation(R.string.info_credit_limit_title, R.string.black_credit_card_credit_limit_information_desc),
            AccountInformation(R.string.info_total_amount_due_title, R.string.black_credit_card_total_amount_due_desc),
            AccountInformation(R.string.info_next_payment_due_title, R.string.black_credit_card_next_payment_due_desc)
        )
    ): InformationData()
    @Parcelize
    data class Arrears(
        override val info: ArrayList<AccountInformation> = arrayListOf(AccountInformation(R.string.account_in_arrears_title, R.string.black_credit_card_account_in_arrears_information_desc),
            AccountInformation(R.string.amount_due_title, R.string.black_credit_card_amount_due_information_desc),
            AccountInformation(R.string.amount_overdue_title, R.string.black_credit_card_amount_overdue_arrears_information_desc),
            AccountInformation(R.string.total_amount_due_title, R.string.black_credit_card_total_amount_due_arrears_desc),
            AccountInformation(R.string.next_payment_due_title, R.string.black_credit_card_next_payment_due_arrears_desc),
            AccountInformation(R.string.balance_at_statement_date_title, R.string.black_credit_card_balance_at_statement_date_desc),
            AccountInformation(R.string.current_balance_title, R.string.black_credit_card_current_balance_arrears_information_desc),
            AccountInformation(R.string.available_funds_title, R.string.black_credit_card_available_fund_arrears_information_desc),
            AccountInformation(R.string.credit_limit_title, R.string.black_credit_card_current_information_arrears_information_desc))
    ): InformationData()

}

@Parcelize
data class AccountInformation(val title: Int?, val description: Int?):Parcelable
