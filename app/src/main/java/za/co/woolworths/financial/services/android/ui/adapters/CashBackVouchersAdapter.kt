package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AvailableVouchersToRedeemListItemBinding
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CashBackVouchers
import za.co.woolworths.financial.services.android.util.WFormatter

class CashBackVouchersAdapter(
    private val cashBackVouchers: ArrayList<CashBackVouchers>
) : RecyclerView.Adapter<CashBackVouchersAdapter.CashBackVouchersViewHolder>() {

    var onItemClick: (() -> Unit)? = null

   inner class CashBackVouchersViewHolder(private val binding: AvailableVouchersToRedeemListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cashBackVoucher: CashBackVouchers) {
            with(binding) {
                itemView.apply {
                    cashBackVoucher.let {
                        voucherSelector.visibility = View.GONE
                        voucherDetails.text = context.getString(R.string.quarterly_voucher)+ " - " +it.details
                        voucherDescription.visibility = View.VISIBLE
                        voucherDescription.text = context.getString(R.string.vouchers_redeemable_at_checkout)
                        expireVoucherDate.visibility = View.VISIBLE
                        expireVoucherDate.text = context.getString(R.string.expires_vouchers,WFormatter.cashBackVoucherDate(it.expiryDate))

                    }
                    setOnClickListener {
                        //TODO: add listener when flag come from backend
                        //onItemClick?.invoke()

                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CashBackVouchersViewHolder(
            AvailableVouchersToRedeemListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = cashBackVouchers.size

    override fun onBindViewHolder(holder: CashBackVouchersViewHolder, position: Int) =
        holder.bind(cashBackVouchers[position])

    fun renderCashBackVouchers(list: List<CashBackVouchers>) {
        cashBackVouchers.clear()
        cashBackVouchers.addAll(list)
    }

}
