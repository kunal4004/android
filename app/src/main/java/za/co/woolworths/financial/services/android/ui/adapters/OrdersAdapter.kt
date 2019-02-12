package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.OrderItem
import za.co.woolworths.financial.services.android.ui.activities.OrderDetailsActivity
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder

class OrdersAdapter(val context: Context, var dataList: ArrayList<OrderItem>) : RecyclerView.Adapter<OrdersBaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): OrdersBaseViewHolder? {
        when (viewType) {
            OrderItem.ViewType.HEADER.value -> {
                return HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_orders_header, parent, false))
            }
            OrderItem.ViewType.UPCOMING_ORDER.value -> {
                return UpcomingOrderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_upcoming_order_item, parent, false))
            }
            OrderItem.ViewType.PAST_ORDER.value -> {
                return PastOrderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_order_item, parent, false))
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

    inner class UpcomingOrderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            itemView.setOnClickListener {
                context.startActivity(Intent(context, OrderDetailsActivity::class.java))
            }
        }

    }

    inner class PastOrderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
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