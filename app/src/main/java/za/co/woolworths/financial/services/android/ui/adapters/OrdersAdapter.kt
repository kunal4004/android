package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.OrderItem
import za.co.woolworths.financial.services.android.ui.activities.OrderDetailsActivity
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder
import kotlinx.android.synthetic.main.my_orders_upcoming_order_item.view.*
import za.co.woolworths.financial.services.android.models.dto.Order
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.WFormatter

class OrdersAdapter(val context: Context, var dataList: ArrayList<OrderItem>) : RecyclerView.Adapter<OrdersBaseViewHolder>() {
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
            itemView.setOnClickListener {
                presentOrderDetailsPage(item)
            }
        }

    }

    inner class PastOrderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as Order
            itemView.orderId.text = item.orderId
            itemView.purchaseDate.text = WFormatter.formatOrdersDate(item.submittedDate)
            itemView.total.text = WFormatter.formatAmount(item.total)
            itemView.setOnClickListener {
                presentOrderDetailsPage(item)
            }
        }

    }

    inner class HeaderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
        }

    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type.value
    }

    private fun presentOrderDetailsPage(item: Order) {
        val intent = Intent(context, OrderDetailsActivity::class.java)
        intent.putExtra("order", item)
        (context as? Activity)?.startActivityForResult(intent, ADD_TO_SHOPPING_LIST_REQUEST_CODE)
        (context as? Activity)?.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
    }
}