package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.text.SpannableString
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.TransactionListChildItemBinding
import za.co.woolworths.financial.services.android.models.dto.account.TransactionItem
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.FontHyperTextParser
import kotlin.math.roundToInt

class TransactionItemViewHolder(val itemBinding: TransactionListChildItemBinding): RecyclerView.ViewHolder(itemBinding.root) {

    fun setTransactionContent(transactionItem: TransactionItem?) {

        itemBinding.apply {
            transactionItem?.apply {
                transactionDate?.text = date
                transactionDescription?.text = description
                transactionAmount?.text = addNegativeSymbolInFront(FontHyperTextParser.getSpannable(amount?.let { amt -> formatTransactionAmount(amt) }, 1))
            }
        }
    }

    private fun formatTransactionAmount(transactionAmount: Float): String? {
        //convert amount to int
        val amount = transactionAmount.roundToInt()
        val formattedAmount: String
        //Convert amount to +ve
        formattedAmount = if (amount < 0) CurrencyFormatter.formatAmountToRandAndCentWithSpace(-amount) else CurrencyFormatter.formatAmountToRandAndCentWithSpace(amount)
        return if (transactionAmount < 0) formattedAmount.replace("R", "R-") else formattedAmount
    }

    private fun addNegativeSymbolInFront(amount: SpannableString): String? {
        var currentAmount = amount.toString()
        if (currentAmount.contains("R-")) {
            currentAmount = currentAmount.replace("R-", "- R")
        }
        return currentAmount
    }


}