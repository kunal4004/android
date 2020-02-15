package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_item_to_shoppinglist_layout.view.*
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder
import kotlinx.android.synthetic.main.my_orders_past_orders_header.view.*
import kotlinx.android.synthetic.main.order_deatils_status_item.view.*
import kotlinx.android.synthetic.main.order_details_commerce_item.view.*
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

class OrderDetailsAdapter(val context: Context, val listner: OnItemClick, var dataList: ArrayList<OrderDetailsItem>) :  RecyclerView.Adapter<OrdersBaseViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersBaseViewHolder {
        when (viewType) {
            OrderDetailsItem.ViewType.HEADER.value -> {
                return HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_orders_header, parent, false))
            }
            OrderDetailsItem.ViewType.ORDER_STATUS.value -> {
                return OrderStatusViewHolder(LayoutInflater.from(context).inflate(R.layout.order_deatils_status_item, parent, false))
            }
            OrderDetailsItem.ViewType.ADD_TO_LIST_LAYOUT.value -> {
                return AddToListViewHolder(LayoutInflater.from(context).inflate(R.layout.add_item_to_shoppinglist_layout, parent, false))
            }
            OrderDetailsItem.ViewType.COMMERCE_ITEM.value -> {
                return OrderItemViewHolder(LayoutInflater.from(context).inflate(R.layout.order_details_commerce_item, parent, false))
            }
            OrderDetailsItem.ViewType.VIEW_TAX_INVOICE.value -> {
                return ViewTaxInvoiceViewHolder(LayoutInflater.from(context).inflate(R.layout.order_details_view_tax_invoice_layout, parent, false))
            }
            OrderDetailsItem.ViewType.CANCEL_ORDER.value -> {
                //isTaxInvoiceViewExist = true
                return CancelOrderViewHolder(LayoutInflater.from(context).inflate(R.layout.cancel_order_layout, parent, false))
            }
        }
        return HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_orders_header, parent, false))
    }

    override fun onBindViewHolder(holder: OrdersBaseViewHolder, position: Int) {
        holder?.bind(position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class OrderStatusViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as OrderDetailsResponse
            itemView.orderState.text = item.orderSummary?.state
            itemView.purchaseDate.text = WFormatter.formatOrdersDate(item.orderSummary?.submittedDate)
            itemView.total.text = WFormatter.formatAmount(item.orderSummary?.total!!)
            itemView.total.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            itemView.noOfItems.text = item?.orderSummary?.totalItemsCount.toString()+if(item?.orderSummary?.totalItemsCount>1)context.getString(R.string.no_of_items) else context.getString(R.string.no_of_item)
            itemView.deliverySuburb.text = item?.orderSummary?.suburb?.name
        }

    }

    inner class OrderItemViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as CommerceItem
            setProductImage(itemView.imProductImage, item.commerceItemInfo.externalImageURL)
            itemView.itemName.text = item.commerceItemInfo.quantity.toString()+" x "+item.commerceItemInfo.productDisplayName
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

    inner class CancelOrderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            //itemView.fakeDivider.visibility = if (isTaxInvoiceViewExist) View.GONE else View.VISIBLE
            itemView.setOnClickListener {
                listner.onCancelOrder()
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

        fun onCancelOrder()
    }

    fun getCommerceItemList(): MutableList<AddToListRequest> {
        val addToListRequest = mutableListOf<AddToListRequest>()
        dataList.forEach {
            if (it.type.name == OrderDetailsItem.ViewType.COMMERCE_ITEM.name) {
                val commerceItem = it.item as? CommerceItem
                commerceItem?.commerceItemInfo?.apply {
                    val listItem = AddToListRequest()
                    listItem.catalogRefId = catalogRefId
                    listItem.skuID = productId
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