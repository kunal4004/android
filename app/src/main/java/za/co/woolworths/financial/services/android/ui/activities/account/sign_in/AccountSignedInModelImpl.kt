package za.co.woolworths.financial.services.android.ui.activities.account.sign_in

import androidx.fragment.app.FragmentActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IAccountSignedInContract
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.AccountHelpInformation

class AccountSignedInModelImpl : IAccountSignedInContract.MyAccountModel {

    private val defaultInformation =
            mutableListOf(AccountHelpInformation(getString(R.string.info_available_funds_title), getString(R.string.black_credit_card_available_fund_information_desc)),
                    AccountHelpInformation(getString(R.string.info_current_balance_title), getString(R.string.black_credit_card_current_balance_information_desc)),
                    AccountHelpInformation(getString(R.string.info_credit_limit_title), getString(R.string.black_credit_card_credit_limit_information_desc)),
                    AccountHelpInformation(getString(R.string.info_total_amount_due_title), getString(R.string.black_credit_card_total_amount_due_desc)),
                    AccountHelpInformation(getString(R.string.info_next_payment_due_title), getString(R.string.black_credit_card_next_payment_due_desc)))

    private val arrearsInformation =
            mutableListOf(AccountHelpInformation(getString(R.string.account_in_arrears_title), getString(R.string.black_credit_card_account_in_arrears_information_desc)),
                    AccountHelpInformation(getString(R.string.amount_due_title), getString(R.string.black_credit_card_amount_due_information_desc)),
                    AccountHelpInformation(getString(R.string.amount_overdue_title), getString(R.string.black_credit_card_amount_overdue_arrears_information_desc)),
                    AccountHelpInformation(getString(R.string.total_amount_due_title), getString(R.string.black_credit_card_total_amount_due_arrears_desc)),
                    AccountHelpInformation(getString(R.string.next_payment_due_title), getString(R.string.black_credit_card_next_payment_due_arrears_desc)),
                    AccountHelpInformation(getString(R.string.balance_at_statement_date_title), getString(R.string.black_credit_card_balance_at_statement_date_desc)),
                    AccountHelpInformation(getString(R.string.current_balance_title), getString(R.string.black_credit_card_current_balance_arrears_information_desc)),
                    AccountHelpInformation(getString(R.string.available_funds_title), getString(R.string.black_credit_card_available_fund_arrears_information_desc)),
                    AccountHelpInformation(getString(R.string.credit_limit_title), getString(R.string.black_credit_card_current_information_arrears_information_desc)))

    override fun getCardProductInformation(accountIsInArrearsState: Boolean): MutableList<AccountHelpInformation> {
        return when (accountIsInArrearsState) {
            false -> defaultInformation
            true -> arrearsInformation
        }
    }

    private fun getString(stringId: Int): String? = (WoolworthsApplication.getInstance()?.currentActivity as? FragmentActivity)?.resources?.getString(stringId)
}