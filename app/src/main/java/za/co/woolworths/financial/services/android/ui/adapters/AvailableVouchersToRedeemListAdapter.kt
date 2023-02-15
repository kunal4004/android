package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AvailableVouchersToRedeemListItemBinding
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.Voucher
import za.co.woolworths.financial.services.android.ui.fragments.voucher_redeemption.VoucherAndPromoCodeContract

class AvailableVouchersToRedeemListAdapter(var vouchers: ArrayList<Voucher>, var listener: VoucherAndPromoCodeContract.AvailableVoucherView) : RecyclerView.Adapter<AvailableVouchersToRedeemListAdapter.VoucherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        return VoucherViewHolder(
            AvailableVouchersToRedeemListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return vouchers.size
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        holder.bind(vouchers[position])
    }

    inner class VoucherViewHolder(val binding: AvailableVouchersToRedeemListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(voucher: Voucher) {
            with(binding) {
                itemView.apply {
                    voucher.let {
                        voucherDetails.text = it.description
                        voucherSelector.isChecked = it.isSelected
                        errorMessage.text = it.errorMessage
                        errorMessage.visibility =
                            if (it.errorMessage.isEmpty()) View.GONE else View.VISIBLE
                        if (voucher.isSelected && voucherSelector.isChecked) {
                            vouchersLayout.setBackgroundResource(R.drawable.bg_selected_voucher)
                        } else {
                            vouchersLayout.setBackgroundResource(R.drawable.bg_un_selected_voucher)
                        }
                    }
                    setOnClickListener {
                        voucher.isSelected = !voucher.isSelected
                        voucher.errorMessage = ""
                        notifyDataSetChanged()
                        listener.enableRedeemButton()
                    }
                }
            }
        }
    }

    fun updateVouchersList(updatedList: ArrayList<Voucher>) {
        this.vouchers = updatedList
        notifyDataSetChanged()
    }

}