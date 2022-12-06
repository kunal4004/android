package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.WrewardsVouchersListItemBinding
import za.co.woolworths.financial.services.android.models.dto.Voucher
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.DateFormatter
import za.co.woolworths.financial.services.android.util.WFormatter
import java.text.ParseException

class WRewardVoucherListViewHolder constructor(val itemBinding: WrewardsVouchersListItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
    constructor(parent: ViewGroup) : this(
        WrewardsVouchersListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    fun setItem(voucher: Voucher) {
        with(voucher) {
            itemBinding.apply {
                voucherValue?.text = when (type ?: "") {
                    "PERCENTAGE" -> WFormatter.formatPercent(amount)
                    else -> WFormatter.formatAmountNoDecimal(amount)
                }

                voucherMessage?.text = description

                root.context?.apply {
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