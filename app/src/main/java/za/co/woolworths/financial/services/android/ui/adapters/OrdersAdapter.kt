package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.my_orders_past_order_item.view.*
import za.co.woolworths.financial.services.android.models.dto.OrderItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder
import kotlinx.android.synthetic.main.order_history_type.view.*
import kotlinx.android.synthetic.main.order_history_type.view.purchaseDate
import za.co.woolworths.financial.services.android.contracts.IPresentOrderDetailInterface
import za.co.woolworths.financial.services.android.models.dto.Order
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
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
                UpcomingOrderViewHolder(LayoutInflater.from(context).inflate(R.layout.order_history_type, parent, false))
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
            itemView.orderNumber?.text = context.getString(R.string.order_id,
                item.orderId.replaceFirstChar { it.uppercase() })
            var itemState = item.state
            if (itemState?.contains(context.getString(R.string.order))) {
                itemState = itemState.replace(context.getString(R.string.order), "")
                itemView.orderState?.text = itemState
            } else {
                itemView.orderState?.text = itemState
            }

            itemView.purchaseDate?.text = WFormatter.formatOrdersHistoryDate(item.submittedDate)
            itemView.orderAmount?.text =
                CurrencyFormatter.formatAmountToRandAndCentWithSpace(item.total)
            itemView.setOnClickListener { iPresentOrderDetailInterface?.presentOrderDetailsPage(item) }
            itemView.orderState.setBackgroundResource(if (item.state.contains(context.getString(R.string.cancelled))) R.drawable.order_state_orange_bg else R.drawable.order_state_bg)

            if (!item.deliveryDates?.isJsonNull!!) {
                val deliveryDates: HashMap<String, String> = hashMapOf()
                deliveryDates.clear()
                itemView.deliveryDateContainer.removeAllViews()
                for (i in 0 until (item.deliveryDates?.asJsonArray?.size() ?: 0)) {
                    deliveryDates.putAll(Gson().fromJson<Map<String, String>>(
                        item.deliveryDates?.asJsonArray?.get(i).toString(), object : TypeToken<Map<String, String>>() {}.type))
                }
                when (deliveryDates.keys.size) {
                    0 -> {
                         itemView.timeslotTitle?.visibility = View.GONE
                         itemView.timeslot?.visibility = View.GONE
                    }
                    1 -> {
                        itemView.timeslot?.text = deliveryDates.getValue(deliveryDates.keys.toList()[0])
                        itemView.timeslotTitle?.visibility = View.VISIBLE
                        itemView.timeslot?.visibility = View.VISIBLE
                    }
                    else -> {
                        itemView.timeslotTitle?.visibility = View.GONE
                        itemView.timeslot?.visibility = View.GONE
                        deliveryDates.entries.forEach { entry ->
                            val view = (context as Activity).layoutInflater.inflate(R.layout.orders_list_delivery_date_item, null)
                            val deliveryItemsType = view.findViewById<TextView>(R.id.deliveryItemsType)
                            val dateOfDelivery = view.findViewById<TextView>(R.id.dateOfDelivery)
                            deliveryItemsType.text = context.getString(R.string.time_slot_value,entry.key)
                            dateOfDelivery.text = entry.value
                           itemView.deliveryDateContainer.addView(view)
                        }
                    }
                }
            }
        }

    }

    inner class PastOrderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as Order
            itemView.orderId?.text = item.orderId
            itemView.purchaseDatePastOrder?.text = WFormatter.formatOrdersDate(item.submittedDate)
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