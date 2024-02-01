package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.TransactionListChildItemBinding
import com.awfs.coordination.databinding.TransactionListParentItemBinding
import za.co.woolworths.financial.services.android.models.dto.account.Transaction
import za.co.woolworths.financial.services.android.models.dto.account.TransactionHeader
import za.co.woolworths.financial.services.android.models.dto.account.TransactionItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.TransactionHeaderViewHolder
import za.co.woolworths.financial.services.android.ui.adapters.holder.TransactionItemViewHolder

class WTransactionAdapter(private val transactionList: MutableList<Transaction>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int = if (transactionList?.get(position) is TransactionItem) 1 else 0

    override fun getItemCount(): Int = transactionList?.size ?: 0

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> TransactionHeaderViewHolder(
                TransactionListParentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            1 -> TransactionItemViewHolder(
                TransactionListChildItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> throw IllegalStateException("Invalid WTransaction viewType found $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        transactionList?.get(position)?.apply {
            when (this) {
                is TransactionHeader -> (holder as? TransactionHeaderViewHolder)?.setTransactionHeader(this as? TransactionHeader, count)
                is TransactionItem -> (holder as? TransactionItemViewHolder)?.setTransactionContent(this as? TransactionItem, headerCount, itemCount)
                else -> throw IllegalStateException("Invalid item type $this")
            }
        }
    }
}