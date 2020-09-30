package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.available_vouchers_to_redeem_list_item.view.*
import za.co.woolworths.financial.services.android.models.dto.voucher_redemption.Voucher
import za.co.woolworths.financial.services.android.ui.fragments.voucher_redeemption.AvailableVoucherContract

class AvailableVouchersToRedeemListAdapter(var vouchers: ArrayList<Voucher>, var listener:AvailableVoucherContract.AvailableVoucherView) : RecyclerView.Adapter<AvailableVouchersToRedeemListAdapter.VoucherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        return VoucherViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.available_vouchers_to_redeem_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return vouchers.size
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        holder.bind(vouchers[position])
    }


    inner class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(voucher: Voucher) {
            itemView.apply {
                voucher.let {
                    voucherDetails.text = it.description
                    voucherSelector.isChecked = it.isSelected
                }
                setOnClickListener {
                    voucher.isSelected = !voucher.isSelected
                    notifyDataSetChanged()
                    listener.onVoucherSelected()
                }
            }

        }
    }

}