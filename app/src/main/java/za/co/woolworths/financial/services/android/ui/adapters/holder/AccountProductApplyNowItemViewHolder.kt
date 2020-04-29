package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IAccountCardItemListener
import za.co.woolworths.financial.services.android.models.dto.Account


class AccountProductApplyNowItemViewHolder(parent: ViewGroup) : WParentItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.account_apply_now_item, parent, false)) {

    fun setAccountContent(account: Account?, accountCardItemListener: IAccountCardItemListener?) {
        with(itemView) {

            setOnClickListener {
                account?.apply {
                }
            }
        }
    }
}