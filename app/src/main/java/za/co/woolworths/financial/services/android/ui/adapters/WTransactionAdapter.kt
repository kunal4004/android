package za.co.woolworths.financial.services.android.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.woolworths.financial.services.android.models.dto.account.Transaction
import za.co.woolworths.financial.services.android.models.dto.account.TransactionHeader
import za.co.woolworths.financial.services.android.models.dto.account.TransactionItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.TransactionHeaderViewHolder
import za.co.woolworths.financial.services.android.ui.adapters.holder.TransactionItemViewHolder
import za.co.woolworths.financial.services.android.ui.adapters.holder.TransactionViewHolder

class WTransactionAdapter(private val transactionList: MutableList<Transaction>?) : RecyclerView.Adapter<TransactionViewHolder>() {

    override fun getItemViewType(position: Int): Int = if (transactionList?.get(position) is TransactionItem) 1 else 0

    override fun getItemCount(): Int = transactionList?.size ?: 0

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        return when (viewType) {
            0 -> TransactionHeaderViewHolder(parent)
            1 -> TransactionItemViewHolder(parent)
            else -> throw IllegalStateException("Invalid WTransaction viewType found $viewType")
        }
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        transactionList?.get(position)?.apply {
            when (this) {
                is TransactionHeader -> (holder as? TransactionHeaderViewHolder)?.setTransactionHeader(this as? TransactionHeader)
                is TransactionItem -> (holder as? TransactionItemViewHolder)?.setTransactionContent(this as? TransactionItem)
                else -> throw IllegalStateException("Invalid item type $this")
            }
        }
    }
}