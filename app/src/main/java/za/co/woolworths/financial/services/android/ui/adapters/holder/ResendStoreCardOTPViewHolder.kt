package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.resend_otp_item.view.*

class ResendStoreCardOTPViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    constructor(parent: ViewGroup) : this(LayoutInflater.from(parent.context).inflate(R.layout.resend_otp_item, parent, false))

    fun setItem(item: Triple<Int, Int, String>?, onClickListener: (Int) -> Unit) {
        with(itemView) {
            context?.apply {
                imOTPNextEmail?.setImageDrawable(item?.first?.let { imageId -> ContextCompat.getDrawable(this, imageId) })
                tvOTPEmailTitle?.text = item?.second?.let { stringId -> resources?.getString(stringId) }
                tvStoreCardNumber.visibility = if (item?.third?.isEmpty() == true) GONE else VISIBLE
                tvStoreCardNumber?.text = item?.third?.let { it }
            }
            setOnClickListener { onClickListener(adapterPosition) }
        }
    }
}