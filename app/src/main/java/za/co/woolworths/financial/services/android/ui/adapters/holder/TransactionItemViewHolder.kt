package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.transaction_list_child_item.view.*
import za.co.woolworths.financial.services.android.models.dto.account.TransactionItem
import za.co.woolworths.financial.services.android.util.FontHyperTextParser
import za.co.woolworths.financial.services.android.util.WFormatter
import kotlin.math.roundToInt

class TransactionItemViewHolder(parent: ViewGroup) : WParentItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transaction_list_child_item, parent, false)) {

    fun setTransactionContent(transactionItem: TransactionItem?) {

        with(itemView) {
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
        formattedAmount = if (amount < 0) WFormatter.formatAmount(-amount) else WFormatter.formatAmount(amount)
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