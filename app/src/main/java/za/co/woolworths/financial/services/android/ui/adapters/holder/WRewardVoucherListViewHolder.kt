package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.wrewards_vouchers_list_item.view.*

import za.co.woolworths.financial.services.android.models.dto.Voucher
import za.co.woolworths.financial.services.android.util.WFormatter
import java.text.ParseException

class WRewardVoucherListViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    constructor(parent: ViewGroup) : this(LayoutInflater.from(parent.context).inflate(R.layout.wrewards_vouchers_list_item, parent, false))

    fun setItem(voucher: Voucher) {
        with(voucher) {
            with(itemView) {
                voucherValue?.text = when (type ?: "") {
                    "PERCENTAGE" -> WFormatter.formatPercent(amount)
                    else -> WFormatter.formatAmountNoDecimal(amount)
                }

                voucherMessage?.text = description

                context?.apply {
                    voucherExpireDate?.text = try {
                        if (WFormatter.isDateExpired(validToDate))
                            getString(R.string.expired) + WFormatter.formatDate(validToDate).toString()
                        else
                            getString(R.string.expires) + WFormatter.formatDate(validToDate).toString()
                    } catch (e: ParseException) {
                        getString(R.string.expires) + validToDate?.toString()
                    }
                }
            }
        }
    }
}