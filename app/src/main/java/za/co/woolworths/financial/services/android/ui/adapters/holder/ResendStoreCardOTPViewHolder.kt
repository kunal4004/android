package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ResendOtpItemBinding

class ResendStoreCardOTPViewHolder(val itemBinding: ResendOtpItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
    fun setItem(item: Triple<Int, Int, String>?, onClickListener: (Int) -> Unit) {
        itemBinding.apply {
            imOTPNextEmail?.setImageDrawable(item?.first?.let { imageId -> ContextCompat.getDrawable(root.context, imageId) })
            tvOTPEmailTitle?.text = item?.second?.let { stringId -> root.resources?.getString(stringId) }
            tvStoreCardNumber.visibility = if (item?.third?.isEmpty() == true) GONE else VISIBLE
            tvStoreCardNumber?.text = item?.third?.let { it }
            root.setOnClickListener { onClickListener(adapterPosition) }
        }
    }
}