package za.co.woolworths.financial.services.android.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pma_manage_card_item.view.*
import za.co.woolworths.financial.services.android.models.dto.GetPaymentMethod
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.util.*

class PMACardsAdapter(private var paymentMethodList: MutableList<GetPaymentMethod>?, val onClickListener: (GetPaymentMethod) -> Unit) : RecyclerView.Adapter<PMACardsAdapter.PMAManageCardItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PMAManageCardItemViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.pma_manage_card_item, parent, false)
        return PMAManageCardItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: PMAManageCardItemViewHolder, position: Int) {
        with(holder) {
            val paymentMethod = paymentMethodList?.get(position)
            bindItems(paymentMethod)

            itemView.setOnClickListener {
                paymentMethodList?.forEach { it.isCardChecked = !it.isCardChecked }
                paymentMethod?.isCardChecked = true
                paymentMethod?.let { item -> onClickListener(item) }
                notifyDataSetChanged()
            }

            itemView.pmaSaveCardImageView?.setImageResource(if (paymentMethod?.isCardChecked == true) R.drawable.checked_item else R.drawable.uncheck_item)
        }
    }

    override fun getItemCount(): Int = paymentMethodList?.size ?: 0

    class PMAManageCardItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(paymentMethod: GetPaymentMethod?) {
            with(itemView) {
                paymentMethod?.apply {
                    showCardVendor(vendor)
                    showCardNumber(cardNumber)
                    showExpiredTag(cardExpired)
                }
            }
        }

        private fun View.showExpiredTag(expirationDate: Boolean?) {
            cardExpiredTextView?.apply {
                visibility = when (expirationDate) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
                KotlinUtils.roundCornerDrawable(this, "#e41f1f")
            }
        }

        private fun View.showCardNumber(cardNumber: String?) {
            cardNumberTextView?.text = cardNumber
        }

        private fun View.showCardVendor(vendor: String?) {
            cardTypeImageView?.setImageResource(when (vendor?.toLowerCase(Locale.getDefault())) {
                "visa" -> R.drawable.card_visa
                "mastercard" -> R.drawable.card_mastercard
                else -> R.drawable.card_visa_grey
            })
        }
    }
}