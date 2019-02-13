package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder
import kotlinx.android.synthetic.main.my_orders_past_orders_header.view.*
import kotlinx.android.synthetic.main.my_orders_upcoming_order_item.view.*
import kotlinx.android.synthetic.main.order_details_commerce_item.view.*
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.Order
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.WFormatter

class OrderDetailsAdapter(val context: Context, var dataList: ArrayList<OrderDetailsItem>) : RecyclerView.Adapter<OrdersBaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): OrdersBaseViewHolder? {
        when (viewType) {
            OrderDetailsItem.ViewType.HEADER.value -> {
                return HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_orders_header, parent, false))
            }
            OrderDetailsItem.ViewType.ORDER_STATUS.value -> {
                return OrderStatusViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_upcoming_order_item, parent, false))
            }
            OrderDetailsItem.ViewType.ADD_TO_LIST_LAYOUT.value -> {
                return AddToListViewHolder(LayoutInflater.from(context).inflate(R.layout.add_item_to_shoppinglist_layout, parent, false))
            }
            OrderDetailsItem.ViewType.COMMERCE_ITEM.value -> {
                return OrderItemViewHolder(LayoutInflater.from(context).inflate(R.layout.order_details_commerce_item, parent, false))
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
            val item = dataList[position].item as Order
            itemView.orderId.text = item.orderId
            itemView.orderState.text = item.state
            itemView.purchaseDate.text = item.submittedDate
            itemView.total.text = WFormatter.formatAmount(item.total)
        }

    }

    inner class OrderItemViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as CommerceItem
            itemView.itemName.text = item.commerceItemInfo.productDisplayName
            itemView.price.text = WFormatter.formatAmount(item.priceInfo.amount)

            itemView.setOnClickListener { presentProductDetailsPage(item) }
        }

    }

    inner class HeaderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as String
            itemView.header.text = item
        }

    }

    inner class AddToListViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
        }

    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type.value
    }

    fun presentProductDetailsPage(commerceItem: CommerceItem) {
        val productList = ProductDetails()
        val commerceItemInfo = commerceItem.commerceItemInfo
        productList.externalImageRef = commerceItemInfo.externalImageURL
        productList.productName = commerceItemInfo.productDisplayName
        productList.fromPrice = commerceItem.priceInfo.getAmount().toFloat()
        productList.productId = commerceItemInfo.productId
        productList.sku = commerceItemInfo.catalogRefId
        val gson = Gson()
        val strProductList = gson.toJson(productList)
        val bundle = Bundle()
        bundle.putString("strProductList", strProductList)
        bundle.putString("strProductCategory", "")
        ScreenManager.presentProductDetails(context as Activity, bundle)
    }
}