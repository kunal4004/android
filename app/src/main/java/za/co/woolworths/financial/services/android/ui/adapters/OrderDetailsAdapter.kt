package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_item_to_shoppinglist_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder
import kotlinx.android.synthetic.main.my_orders_past_orders_header.view.*
import kotlinx.android.synthetic.main.my_orders_upcoming_order_item.view.*
import kotlinx.android.synthetic.main.order_details_commerce_item.view.*
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.Order
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

class OrderDetailsAdapter(val context: Context, val listner: OnItemClick, var dataList: ArrayList<OrderDetailsItem>) : RecyclerView.Adapter<OrdersBaseViewHolder>() {

    var isTaxInvoiceViewExist: Boolean = false

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
            OrderDetailsItem.ViewType.VIEW_TAX_INVOICE.value -> {
                isTaxInvoiceViewExist = true
                return ViewTaxInvoiceViewHolder(LayoutInflater.from(context).inflate(R.layout.order_details_view_tax_invoice_layout, parent, false))
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
            itemView.orderState.setBackgroundResource(0)
            itemView.orderState.setTextColor(ContextCompat.getColor(context, R.color.black))
            itemView.orderState.text = item.state
            itemView.purchaseDate.text = WFormatter.formatOrdersDate(item.submittedDate)
            itemView.total.text = WFormatter.formatAmount(item.total)
            itemView.total.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }

    }

    inner class OrderItemViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as CommerceItem
            setProductImage(itemView.productImage, item.commerceItemInfo.externalImageURL)
            itemView.itemName.text = item.commerceItemInfo.productDisplayName
            itemView.price.text = WFormatter.formatAmount(item.priceInfo.amount)
            itemView.price.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            itemView.setOnClickListener { listner.onOpenProductDetail(item) }
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
            itemView.fakeDivider.visibility = if (isTaxInvoiceViewExist) View.GONE else View.VISIBLE
            itemView.setOnClickListener {
                listner.onAddToList(getCommerceItemList())
            }
        }

    }

    inner class ViewTaxInvoiceViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {

            itemView.setOnClickListener {
                listner.onViewTaxInvoice()
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type.value
    }


    interface OnItemClick {

        fun onAddToList(commerceItemList: MutableList<AddToListRequest>)

        fun onOpenProductDetail(commerceItem: CommerceItem)

        fun onViewTaxInvoice()
    }

    fun getCommerceItemList(): MutableList<AddToListRequest> {
        val addToListRequest = mutableListOf<AddToListRequest>()
        dataList.forEach {
            if (it.type.name.equals(OrderDetailsItem.ViewType.COMMERCE_ITEM.name)) {
                val commerceItem = it.item as? CommerceItem
                commerceItem?.commerceItemInfo?.apply {
                    val listItem = AddToListRequest()
                    listItem.catalogRefId = catalogRefId
                    listItem.skuID = catalogRefId
                    listItem.giftListId = catalogRefId
                    listItem.quantity = getQuantity().toString()
                    addToListRequest.add(listItem)
                }
            }
        }
        return addToListRequest
    }

    private fun setProductImage(image: WrapContentDraweeView, imgUrl: String) {
        if (!TextUtils.isEmpty(imgUrl)) {
            image.setResizeImage(true)
            image.setImageURI(Utils.getExternalImageRef() + imgUrl + if (imgUrl.indexOf("?") > 0) "w=" + 48 + "&q=" + 48 else "?w=" + 48 + "&q=" + 48)
        }
    }
}