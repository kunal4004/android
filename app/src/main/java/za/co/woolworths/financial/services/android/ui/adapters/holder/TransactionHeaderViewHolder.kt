package za.co.woolworths.financial.services.android.ui.adapters.holder

import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.TransactionListParentItemBinding
import za.co.woolworths.financial.services.android.models.dto.account.TransactionHeader

class TransactionHeaderViewHolder(val itemBinding: TransactionListParentItemBinding): RecyclerView.ViewHolder(itemBinding.root) {
    fun setTransactionHeader(transactionHeader: TransactionHeader?, position: Int) {
        itemBinding.transactionMonth?.text =  transactionHeader?.monthYear ?: ""
        itemBinding.transactionMonth.contentDescription = "month_${position}_title"

    }
}