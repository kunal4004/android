package za.co.woolworths.financial.services.android.ui.adapters

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.OrderListItemBinding
import za.co.woolworths.financial.services.android.models.dto.cart.OrderItem
import za.co.woolworths.financial.services.android.util.CurrencyFormatter

class ItemsOrderListAdapter(var items: ArrayList<OrderItem>) : RecyclerView.Adapter<ItemsOrderListAdapter.ItemsOrderListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsOrderListViewHolder {
        return ItemsOrderListViewHolder(
            OrderListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemsOrderListViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemsOrderListViewHolder(val itemBinding: OrderListItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(orderItem: OrderItem) {
            itemBinding.apply {
                itemImageView.setImageURI(orderItem.commerceItemInfo?.externalImageRefV2);

                val itemDescriptionSpan: Spannable = SpannableString(
                    orderItem.commerceItemInfo?.quantity.toString()
                        .plus(" X ")
                        .plus(orderItem.commerceItemInfo?.productDisplayName)
                )
                itemDescriptionSpan.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    orderItem.commerceItemInfo?.quantity.toString().length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                itemDescription.text = itemDescriptionSpan

                itemPrice.text = CurrencyFormatter
                    .formatAmountToRandAndCentWithSpace(orderItem.priceInfo?.amount)
            }
        }
    }
}
