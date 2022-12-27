package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.PmaManageCardItemBinding
import za.co.woolworths.financial.services.android.models.dto.GetPaymentMethod
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.util.*

class PMACardsAdapter(private var paymentMethodList: MutableList<GetPaymentMethod>?, val onClickListener: (GetPaymentMethod, Int) -> Unit) : RecyclerView.Adapter<PMACardsAdapter.PMAManageCardItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PMAManageCardItemViewHolder {
        return PMAManageCardItemViewHolder(
            PmaManageCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PMAManageCardItemViewHolder, position: Int) {
        with(holder) {
            val paymentMethod = paymentMethodList?.get(position)
            bindItems(paymentMethod)

            itemBinding.root.setOnClickListener {
                paymentMethod?.apply {
                    if (!cardExpired) {
                        paymentMethodList?.forEach { it.isCardChecked = false }
                        isCardChecked = true
                    }
                    onClickListener(this, adapterPosition)
                    notifyDataSetChanged()
                }
            }

            itemBinding.pmaSaveCardImageView?.setImageResource(if (paymentMethod?.isCardChecked == true) R.drawable.checked_item else R.drawable.uncheck_item)
        }
    }

    override fun getItemCount(): Int {
        // ensure only 10 cards are visible
        val size = paymentMethodList?.size ?: 0
        return if (size >= 10) 10 else size
    }

    class PMAManageCardItemViewHolder(val itemBinding: PmaManageCardItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItems(paymentMethod: GetPaymentMethod?) {
            itemBinding.apply {
                paymentMethod?.apply {
                    showCardVendor(vendor)
                    showCardNumber(cardNumber)
                    showExpiredTag(cardExpired)
                }
            }
        }

        private fun PmaManageCardItemBinding.showExpiredTag(expirationDate: Boolean?) {
            cardExpiredTextView?.apply {
                visibility = when (expirationDate) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
                KotlinUtils.roundCornerDrawable(this, "#e41f1f")
            }
        }

        private fun PmaManageCardItemBinding.showCardNumber(cardNumber: String?) {
            cardNumberTextView?.text = cardNumber
        }

        private fun PmaManageCardItemBinding.showCardVendor(vendor: String?) {
            cardTypeImageView?.setImageResource(when (vendor?.toLowerCase(Locale.getDefault())) {
                "visa" -> R.drawable.card_visa
                "mastercard" -> R.drawable.card_mastercard
                else -> R.drawable.card_visa_grey
            })
        }
    }

    fun getList() = paymentMethodList

    fun notifyUpdate(items: MutableList<GetPaymentMethod>?, selectedPosition: Int) {
        if (items?.size ?: 0 > 0) {
            items?.forEach { it.isCardChecked = false }
            items?.get(selectedPosition)?.isCardChecked = true
        }
        this.paymentMethodList = items
        notifyDataSetChanged()
    }

    fun notifyInsert(paymentMethod: GetPaymentMethod, position: Int) {
        if (paymentMethod.isCardChecked)
            paymentMethodList?.forEach { it.isCardChecked = false }
        paymentMethodList?.add(position, paymentMethod)
        notifyItemInserted(position)
        notifyDataSetChanged()
    }

}