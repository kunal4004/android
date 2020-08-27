package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.models.dto.OrderItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder
import kotlinx.android.synthetic.main.my_orders_upcoming_order_item.view.*
import za.co.woolworths.financial.services.android.contracts.IPresentOrderDetailInterface
import za.co.woolworths.financial.services.android.models.dto.Order
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.WFormatter

class OrdersAdapter(val context: Context, val iPresentOrderDetailInterface: IPresentOrderDetailInterface?, var dataList: ArrayList<OrderItem>) : RecyclerView.Adapter<OrdersBaseViewHolder>() {
    override fun onBindViewHolder(holder: OrdersBaseViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersBaseViewHolder {
        return when (viewType) {
            OrderItem.ViewType.HEADER.value -> {
                HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_orders_header, parent, false))
            }
            OrderItem.ViewType.UPCOMING_ORDER.value -> {
                UpcomingOrderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_upcoming_order_item, parent, false))
            }
            OrderItem.ViewType.PAST_ORDER.value -> {
                PastOrderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_order_item, parent, false))
            }
            else -> HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_orders_header, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class UpcomingOrderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as Order
            itemView.orderId.text = item.orderId
            itemView.orderState.text = item.state
            itemView.purchaseDate.text = WFormatter.formatOrdersDate(item.submittedDate)
            itemView.total.text = WFormatter.formatAmount(item.total)
            itemView.setOnClickListener { iPresentOrderDetailInterface?.presentOrderDetailsPage(item) }
            itemView.orderState.setBackgroundResource(if (item.state.equals("Order Cancelled", true)) R.drawable.order_state_orange_bg else R.drawable.order_state_bg)
            itemView.deliveryItemsType.text = bindString(if (item.clickAndCollectOrder) R.string.collection_date else R.string.delivery_date)
            itemView.deliveryItemsType.contentDescription = bindString(if (item.clickAndCollectOrder) R.string.collection_date_title else R.string.delivery_date_title)
            itemView.deliveryDate.contentDescription = bindString(if (item.clickAndCollectOrder) R.string.collection_date_value else R.string.delivery_date_value)
            if (!item.deliveryDates.isJsonNull) {
                val deliveryDates: HashMap<String, String> = hashMapOf()
                deliveryDates.clear()
                itemView.deliveryDateContainer.removeAllViews()
                for (i in 0 until item.deliveryDates.asJsonArray.size()) {
                    deliveryDates.putAll(Gson().fromJson<Map<String, String>>(item.deliveryDates.asJsonArray.get(i).toString(), object : TypeToken<Map<String, String>>() {}.type))
                }
                when (deliveryDates.keys.size) {
                    0 -> {
                        itemView.deliveryDateLayout.visibility = View.GONE
                    }
                    1 -> {
                        itemView.deliveryDate.text = deliveryDates.getValue(deliveryDates.keys.toList()[0])
                        itemView.deliveryDate.visibility = View.VISIBLE
                        itemView.deliveryDateLayout.visibility = View.VISIBLE
                    }
                    else -> {
                        itemView.deliveryDate.visibility = View.GONE
                        deliveryDates.entries.forEach { entry ->
                            val view = (context as Activity).layoutInflater.inflate(R.layout.orders_list_delivery_date_item, null)
                            val deliveryItemsType = view.findViewById<WTextView>(R.id.deliveryItemsType)
                            val dateOfDelivery = view.findViewById<WTextView>(R.id.dateOfDelivery)
                            deliveryItemsType.text = entry.key
                            dateOfDelivery.text = entry.value
                            itemView.deliveryDateContainer.addView(view)
                        }
                        itemView.deliveryDateLayout.visibility = View.VISIBLE
                    }
                }
            } else {
                itemView.deliveryDateLayout.visibility = View.GONE
            }

        }

    }

    inner class PastOrderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as Order
            itemView.orderId.text = item.orderId
            itemView.purchaseDate.text = WFormatter.formatOrdersDate(item.submittedDate)
            itemView.total.text = WFormatter.formatAmount(item.total)
            itemView.setOnClickListener { iPresentOrderDetailInterface?.presentOrderDetailsPage(item) }
        }

    }

    inner class HeaderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
        }

    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type.value
    }

}