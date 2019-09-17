package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.resend_otp_item.view.*
import za.co.woolworths.financial.services.android.util.KotlinUtils

class ResendStoreCardOTPViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    constructor(parent: ViewGroup) : this(LayoutInflater.from(parent.context).inflate(R.layout.resend_otp_item, parent, false))

    fun setItem(item: Pair<Int, Int>?, onClickListener: (Int) -> Unit) {
        with(itemView) {
            context?.apply {
                imOTPNextEmail?.setImageDrawable(item?.first?.let { imageId -> ContextCompat.getDrawable(this, imageId) })
                item?.second?.let { title -> KotlinUtils.contactCustomerCare(this, SpannableString(resources?.getString(title)), "0861 50 20 20", tvOTPEmailTitle,false)}
            }
            setOnClickListener { onClickListener(adapterPosition) }
        }
    }
}