package za.co.woolworths.financial.services.android.ui.fragments.loan

import android.app.Activity
import android.support.v4.app.Fragment
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.util.FontHyperTextParser
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

open class LoanBaseFragment : Fragment() {

    var arrowIsVisible = false

    private fun getPersonalLoanInfo(): Account? {
        if (arguments != null) {
            if (arguments.containsKey(LoanWithdrawalFragment.PERSONAL_LOAN_INFO)) {
                return Gson().fromJson(arguments.get(LoanWithdrawalFragment.PERSONAL_LOAN_INFO) as String, Account::class.java)
            }
        }
        return null
    }

    fun getAvailableFund(): Int {
        return getPersonalLoanInfo()?.availableFunds ?: return 0
    }

    fun getCreditLimit(): Int {
        return getPersonalLoanInfo()?.creditLimit ?: return 0
    }

    fun getMinDrawnAmountWithoutCent(): Int {
        return (getPersonalLoanInfo()?.minDrawDownAmount ?: return 0) / 100
    }

    fun getMinDrawnAmountWithCent(): Int {
        return (getPersonalLoanInfo()?.minDrawDownAmount ?: return 0)
    }

    fun getProductOfferingId(): Int {
        return (getPersonalLoanInfo()?.productOfferingId ?: return 0)
    }

    fun finishActivity(activity: Activity) {
        activity.finish()
        activity.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    fun currencyFormatter(amount: Int, activity: Activity): String = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(amount), 1, activity))

    fun amountToInt(formattedAmount: String) = formattedAmount.substring(0, formattedAmount.indexOf(".")).replace("[\\D]".toRegex(), "")

    fun repaymentPeriod(amount: Int): Int = if (amount < 1000000) 36 else 60

}