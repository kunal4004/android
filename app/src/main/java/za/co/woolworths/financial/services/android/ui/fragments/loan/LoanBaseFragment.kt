package za.co.woolworths.financial.services.android.ui.fragments.loan

import android.app.Activity
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.util.FontHyperTextParser
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

open class LoanBaseFragment : Fragment() {

    var arrowIsVisible = false

    private fun getPersonalLoanInfo(): Account? {
        return (arguments?.get(LoanWithdrawalFragment.PERSONAL_LOAN_INFO) as? String)?.let {  Gson().fromJson(it, Account::class.java)}
    }

    private fun getRpCreditLimitThreshold(): Int {
        return (getPersonalLoanInfo()?.rpCreditLimitThreshold)?.div(100) ?: 0
    }

    private fun getCreditAmount(): Int {
        return getPersonalLoanInfo()?.creditLimit?.div(100) ?: 0
    }

    fun getAvailableFund(): Int {
        return getPersonalLoanInfo()?.availableFunds ?: return 0
    }

    fun getAvailableFundWithoutCent(): Int {
        return ((getPersonalLoanInfo()?.availableFunds) ?: return 0).div(100)
    }

    fun getCreditLimit(): Int {
        return getPersonalLoanInfo()?.creditLimit ?: return 0
    }

    fun getMinDrawnAmountWithoutCent(): Int {
        return ((getPersonalLoanInfo()?.minDrawDownAmount) ?: return 0).div(100)
    }


    fun getProductOfferingId(): Int {
        return (getPersonalLoanInfo()?.productOfferingId ?: return 0)
    }

    fun finishActivity(activity: Activity?) {
        activity?.apply {
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        }
    }

    fun currencyFormatter(amount: Int, activity: Activity): String = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(amount), 1, activity))

    fun amountToInt(formattedAmount: String) = formattedAmount.substring(0, formattedAmount.indexOf(".")).replace("[\\D]".toRegex(), "")

    fun repaymentPeriod(): Int = if (getCreditAmount() >= getRpCreditLimitThreshold()) 60 else 36
}