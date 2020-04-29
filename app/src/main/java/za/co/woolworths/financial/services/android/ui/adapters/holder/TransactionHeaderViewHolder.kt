package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.transaction_list_parent_item.view.*
import za.co.woolworths.financial.services.android.models.dto.account.TransactionHeader

class TransactionHeaderViewHolder(parent: ViewGroup) : WParentItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transaction_list_parent_item, parent, false)) {
    fun setTransactionHeader(transactionHeader: TransactionHeader?) {
        itemView.transactionMonth?.text =  transactionHeader?.monthYear ?: ""
    }
}