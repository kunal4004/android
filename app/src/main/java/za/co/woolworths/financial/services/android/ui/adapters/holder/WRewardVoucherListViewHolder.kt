package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.wrewards_vouchers_list_item.view.*

import za.co.woolworths.financial.services.android.models.dto.Voucher
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.DateFormatter
import za.co.woolworths.financial.services.android.util.WFormatter
import java.lang.IllegalArgumentException
import java.text.ParseException

class WRewardVoucherListViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    constructor(parent: ViewGroup) : this(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.wrewards_vouchers_list_item, parent, false)
    )

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
                        if (DateFormatter.isDateExpired(validToDate))
                            bindString(R.string.expired) + DateFormatter.formatDate(validToDate)
                        else
                            bindString(R.string.expires) + DateFormatter.formatDate(validToDate)
                    } catch (e: ParseException) {
                        bindString(R.string.expires) + validToDate?.toString()
                    } catch (e: IllegalArgumentException) {
                        bindString(R.string.expires) + validToDate?.toString()
                    }
                }
            }
        }
    }
}