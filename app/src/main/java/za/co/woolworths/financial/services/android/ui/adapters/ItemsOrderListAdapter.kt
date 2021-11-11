package za.co.woolworths.financial.services.android.ui.adapters

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.order_list_item.view.*
import za.co.woolworths.financial.services.android.models.dto.cart.OrderItem
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils

class ItemsOrderListAdapter(var items: ArrayList<OrderItem>) : RecyclerView.Adapter<ItemsOrderListAdapter.ItemsOrderListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsOrderListViewHolder {
        return ItemsOrderListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.order_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ItemsOrderListViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemsOrderListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parameter = "w=85&q=85"
        fun bind(orderItem: OrderItem) {
            itemView.itemImageView.setImageURI(orderItem.commerceItemInfo?.externalImageURLV2 + parameter);

            val itemDescriptionSpan: Spannable = SpannableString(orderItem.commerceItemInfo?.quantity.toString()
                .plus(" X ")
                .plus(orderItem.commerceItemInfo?.productDisplayName))
            itemDescriptionSpan.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                orderItem.commerceItemInfo?.quantity.toString().length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            itemView.itemDescription.text =  itemDescriptionSpan

            itemView.itemPrice.text = CurrencyFormatter
                .formatAmountToRandAndCentWithSpace(orderItem.priceInfo?.amount)
        }
    }
}
