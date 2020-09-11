package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R

class AvailableVouchersToRedeemListAdapter : RecyclerView.Adapter<AvailableVouchersToRedeemListAdapter.VoucherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        return VoucherViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.available_vouchers_to_redeem_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
    }


    class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

}