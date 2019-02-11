package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.OrderItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder

class OrderDetailsAdapter(val context: Context, var dataList: ArrayList<OrderItem>) : RecyclerView.Adapter<OrdersBaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): OrdersBaseViewHolder? {
        when (viewType) {
            OrderItem.ViewType.HEADER.value -> {
                return HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_orders_header, parent, false))
            }
            OrderItem.ViewType.UPCOMING_ORDER.value -> {
                return OrderStatusViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_upcoming_order_item, parent, false))
            }
            OrderItem.ViewType.PAST_ORDER.value -> {
                return OrderItemViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_order_item, parent, false))
            }
            OrderItem.ViewType.PAST_ORDER.value -> {
                return AddToListViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_order_item, parent, false))
            }
        }
        return null
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: OrdersBaseViewHolder?, position: Int) {
        holder?.bind(position)
    }

    inner class OrderStatusViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
        }

    }

    inner class OrderItemViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
        }

    }

    inner class HeaderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
        }

    }

    inner class AddToListViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
        }

    }
}