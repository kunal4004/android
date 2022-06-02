package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds

import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IAccountProductLandingDao
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import javax.inject.Inject

interface IUserAccountBalance {
    fun getAvailableFunds(): String
    fun getCurrentBalance(): String
    fun getCreditLimit(): String
    fun getAmountOverdue(): String
    fun getTotalAmountDue(): String
    fun getPaymentDueDate(): String
}

class UserAccountBalance @Inject constructor(accountDao: AccountProductLandingDao) :
    IUserAccountBalance, IAccountProductLandingDao by accountDao {

    override fun getAvailableFunds(): String = formatCurrency(product?.availableFunds)
    override fun getCurrentBalance(): String = formatCurrency(product?.currentBalance)
    override fun getCreditLimit(): String = formatCurrency(product?.creditLimit)
    override fun getAmountOverdue(): String = formatCurrency(product?.amountOverdue)
    override fun getTotalAmountDue(): String = formatCurrency(product?.totalAmountDue)
    override fun getPaymentDueDate(): String = formatDate(product?.paymentDueDate)

    private fun formatCurrency(amount: Int?): String {
        return Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCentWithSpace(amount))
    }

    private fun formatDate(date: String?): String {
        date ?: return "N/A"
        return WFormatter.addSpaceToDate(WFormatter.newDateFormat(date))
    }
}