package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.CashBackVouchersInfoListItemBinding
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CashBackVouchersInfo


class CashBackInfoIndicatorAdapter(
    private val cashBackVouchersInfo: ArrayList<CashBackVouchersInfo>,
) : RecyclerView.Adapter<CashBackInfoIndicatorAdapter.CashBackVouchersInfoViewHolder>() {


    class CashBackVouchersInfoViewHolder(private val binding: CashBackVouchersInfoListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cashBackVouchers: CashBackVouchersInfo) {
            with(binding) {
                itemView.apply {
                    cashBackVouchers.let {
                        voucherInfoText.text = it.infoTitle
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CashBackVouchersInfoViewHolder(
            CashBackVouchersInfoListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = cashBackVouchersInfo.size

    override fun onBindViewHolder(holder: CashBackVouchersInfoViewHolder, position: Int) =
        holder.bind(cashBackVouchersInfo[position])

    fun renderCashBackVouchersInfo(list: List<CashBackVouchersInfo>) {
        cashBackVouchersInfo.clear()
        cashBackVouchersInfo.addAll(list)
    }

}