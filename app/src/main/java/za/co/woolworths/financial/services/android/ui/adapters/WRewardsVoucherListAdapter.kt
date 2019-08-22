package za.co.woolworths.financial.services.android.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.woolworths.financial.services.android.models.dto.Voucher
import za.co.woolworths.financial.services.android.ui.adapters.holder.WRewardVoucherListViewHolder

class WRewardsVoucherListAdapter : RecyclerView.Adapter<WRewardVoucherListViewHolder>() {
    var vouchers: List<Voucher>? = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WRewardVoucherListViewHolder {
        return WRewardVoucherListViewHolder(parent)
    }

    override fun onBindViewHolder(holder: WRewardVoucherListViewHolder, position: Int) {
        vouchers?.get(position)?.apply { holder.setItem(this) }
    }

    fun setItem(vouchers: List<Voucher>) {
        this.vouchers = vouchers
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = vouchers?.size ?: 0

}