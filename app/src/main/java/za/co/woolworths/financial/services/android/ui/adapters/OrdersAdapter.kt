package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.runtime.remember
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.MyOrdersPastOrderItemBinding
import com.awfs.coordination.databinding.MyOrdersPastOrdersHeaderBinding
import com.awfs.coordination.databinding.OrderHistoryTypeBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.contracts.IPresentOrderDetailInterface
import za.co.woolworths.financial.services.android.models.dto.Order
import za.co.woolworths.financial.services.android.models.dto.OrderItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder
import za.co.woolworths.financial.services.android.ui.views.order_again.OrderState
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color4ABB77
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD85C11
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorF3662D
import za.co.woolworths.financial.services.android.ui.wfs.theme.ErrorLabel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.WFormatter

class OrdersAdapter(val context: Context, val iPresentOrderDetailInterface: IPresentOrderDetailInterface?, var dataList: ArrayList<OrderItem>) : RecyclerView.Adapter<OrdersBaseViewHolder>() {
    override fun onBindViewHolder(holder: OrdersBaseViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersBaseViewHolder {
        return when (viewType) {
            OrderItem.ViewType.HEADER.value -> {
                HeaderViewHolder(
                    MyOrdersPastOrdersHeaderBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            OrderItem.ViewType.UPCOMING_ORDER.value -> {
                UpcomingOrderViewHolder(
                    OrderHistoryTypeBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            OrderItem.ViewType.PAST_ORDER.value -> {
                PastOrderViewHolder(
                    MyOrdersPastOrderItemBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            else -> HeaderViewHolder(
                MyOrdersPastOrdersHeaderBinding.inflate(LayoutInflater.from(context), parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class UpcomingOrderViewHolder(val itemBinding: OrderHistoryTypeBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            val item = dataList[position].item as Order
            itemBinding.orderState.setContent {
                OneAppTheme {
                    val background = remember {
                        when {
                            item.state?.contains(context.getString(R.string.cancelled)) == true && item.endlessAisleOrder -> ErrorLabel
                            item.state?.contains(context.getString(R.string.cancelled)) == true -> ColorF3662D
                            item.endlessAisleOrder -> ColorD85C11
                            else -> Color4ABB77
                        }
                    }

                    OrderState(
                        context.getString(R.string.order_id, item.orderId.replaceFirstChar { it.uppercase() }),
                        item.state?.replace(context.getString(R.string.order), "") ?: "",
                        errorLabel = "",
                        background
                    )
                }
            }

            itemBinding.purchaseDate?.text = WFormatter.formatOrdersHistoryDate(item.submittedDate)
            itemBinding.orderAmount?.text =
                CurrencyFormatter.formatAmountToRandAndCentWithSpace(item.total)
            itemBinding.root.setOnClickListener { iPresentOrderDetailInterface?.presentOrderDetailsPage(item) }

            if (!item.deliveryDates?.isJsonNull!!) {
                val deliveryDates: HashMap<String, String> = hashMapOf()
                deliveryDates.clear()
                itemBinding.deliveryDateContainer.removeAllViews()
                for (i in 0 until (item.deliveryDates?.asJsonArray?.size() ?: 0)) {
                    deliveryDates.putAll(Gson().fromJson<Map<String, String>>(
                        item.deliveryDates?.asJsonArray?.get(i).toString(), object : TypeToken<Map<String, String>>() {}.type))
                }
                when (deliveryDates.keys.size) {
                    0 -> {
                         itemBinding.timeslotTitle?.visibility = View.GONE
                         itemBinding.timeslot?.visibility = View.GONE
                    }
                    1 -> {
                        itemBinding.timeslot?.text = deliveryDates.getValue(deliveryDates.keys.toList()[0])
                        itemBinding.timeslotTitle?.visibility = View.VISIBLE
                        itemBinding.timeslot?.visibility = View.VISIBLE
                    }
                    else -> {
                        itemBinding.timeslotTitle?.visibility = View.GONE
                        itemBinding.timeslot?.visibility = View.GONE
                        deliveryDates.entries.forEach { entry ->
                            val view = (context as Activity).layoutInflater.inflate(R.layout.orders_list_delivery_date_item, null)
                            val deliveryItemsType = view.findViewById<TextView>(R.id.deliveryItemsType)
                            val dateOfDelivery = view.findViewById<TextView>(R.id.dateOfDelivery)
                            deliveryItemsType.text = context.getString(R.string.time_slot_value,entry.key)
                            dateOfDelivery.text = entry.value
                           itemBinding.deliveryDateContainer.addView(view)
                        }
                    }
                }
            }
        }

    }

    inner class PastOrderViewHolder(val itemBinding: MyOrdersPastOrderItemBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            val item = dataList[position].item as Order
            itemBinding.orderId?.text = item.orderId
            itemBinding.purchaseDatePastOrder?.text = WFormatter.formatOrdersDate(item.submittedDate)
            itemBinding.root.setOnClickListener { iPresentOrderDetailInterface?.presentOrderDetailsPage(item) }
        }

    }

    inner class HeaderViewHolder(val itemBinding: MyOrdersPastOrdersHeaderBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
        }

    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type.value
    }

}