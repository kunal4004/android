package za.co.woolworths.financial.services.android.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.woolworths.financial.services.android.contracts.IAccountCardItemListener
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.ui.adapters.holder.*
import java.util.*

class WAccountProductAdapter(private val accountCardItemListener: IAccountCardItemListener, val accountList: MutableList<Account>?) : RecyclerView.Adapter<WParentItemViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (accountList?.get(position)?.productGroupCode?.toLowerCase(Locale.getDefault())) {
            "cc", "pl", "sc" -> 1
            else -> 0
        }
    }

    override fun getItemCount(): Int = accountList?.size ?: 0

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WParentItemViewHolder {
        return when (viewType) {
            0 -> AccountProductApplyNowItemViewHolder(parent)
            1 -> AccountProductLinkedItemViewHolder(parent)
            else -> throw IllegalStateException("Invalid product item viewType found $viewType")
        }
    }

    override fun onBindViewHolder(holder: WParentItemViewHolder, position: Int) {
        accountList?.get(position)?.apply {
            when (this.productGroupCode.toLowerCase(Locale.getDefault())) {
                "cc", "pl", "sc" -> (holder as? AccountProductLinkedItemViewHolder)?.setAccountContent(this,accountCardItemListener)
                else -> (holder as? AccountProductApplyNowItemViewHolder)?.setAccountContent(this,accountCardItemListener)
            }
        }
    }
}