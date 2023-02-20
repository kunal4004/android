package za.co.woolworths.financial.services.android.ui.views.actionsheet.vouchersBottomDialog

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CashBackVouchersInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CashBackVouchersInfo
import za.co.woolworths.financial.services.android.ui.adapters.CashBackInfoIndicatorAdapter
import javax.inject.Inject

class VouchersBottomDialogImpl @Inject constructor() : VouchersBottomDialog {
    override fun showCashBackVouchersInfo(
        context: Context,
        title: String,
        desc: String,
        isVouchersInfo: Boolean,
    ) {
        val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val binding = CashBackVouchersInfoBinding.inflate(dialog.layoutInflater, null, false)

        binding.apply {
            if (isVouchersInfo) {
                val cashBackInfoIndicatorAdapter = CashBackInfoIndicatorAdapter(ArrayList())
                cashBackInfoRecyclerView.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = cashBackInfoIndicatorAdapter
                }
                cashBackInfoIndicatorAdapter.renderCashBackVouchersInfo(getCashBackVouchersInfoList(
                    context))
                cashBackInfoIndicatorAdapter.notifyDataSetChanged()
            } else {
                cashBackInfoRecyclerView.visibility = View.GONE
                tvDesc.visibility = View.VISIBLE
                tvDesc.text = desc
            }
            tvTitle.text = title
            gotItButton.setOnClickListener {
                dialog.dismiss()
            }
            dialog.setContentView(root)
        }
        dialog.show()
    }

    private fun getCashBackVouchersInfoList(context: Context): List<CashBackVouchersInfo> {
        return listOf(CashBackVouchersInfo(context.getString(R.string.cash_back_vouchers_info_one)),
            CashBackVouchersInfo(context.getString(R.string.cash_back_vouchers_info_two)),
            CashBackVouchersInfo(context.getString(R.string.cash_back_vouchers_info_three)),
            CashBackVouchersInfo(context.getString(R.string.cash_back_vouchers_info_four)))
    }
}
