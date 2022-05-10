package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder
import kotlinx.android.synthetic.main.my_orders_past_orders_header.view.*
import kotlinx.android.synthetic.main.order_deatils_status_item.view.*
import kotlinx.android.synthetic.main.order_details_commerce_item.view.*
import kotlinx.android.synthetic.main.order_details_gift_commerce_item.view.*
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.wenum.Delivery

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

            OrderDetailsItem.ViewType.GIFT.value -> {
                return GiftViewHolder(LayoutInflater.from(context).inflate(R.layout.order_details_gift_commerce_item, parent, false))
            }

            OrderDetailsItem.ViewType.COMMERCE_ITEM.value -> {
                return OrderItemViewHolder(LayoutInflater.from(context).inflate(R.layout.order_details_commerce_item, parent, false))
            }
            OrderDetailsItem.ViewType.VIEW_TAX_INVOICE.value -> {
                return ViewTaxInvoiceViewHolder(LayoutInflater.from(context).inflate(R.layout.order_details_view_tax_invoice_layout, parent, false))
            }
            OrderDetailsItem.ViewType.CANCEL_ORDER.value -> {
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
            itemView.purchaseDate.text =
                WFormatter.formatOrdersDate(item.orderSummary?.submittedDate)
            itemView.total.text =
                CurrencyFormatter.formatAmountToRandAndCentWithSpace(item.orderSummary?.total!!)
            itemView.total.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            itemView.noOfItems.text =
                item?.orderSummary?.totalItemsCount.toString() + if (item?.orderSummary?.totalItemsCount > 1) context.getString(
                    R.string.no_of_items
                ) else context.getString(R.string.no_of_item)

            val delivery: String? = item.orderSummary?.fulfillmentDetails?.deliveryType
            var deliveryType: Delivery?
            if(delivery.isNullOrEmpty()) {

                val storePickup = item.orderSummary?.store != null
                deliveryType = if (storePickup) Delivery.CNC else Delivery.STANDARD

                if (item?.orderSummary?.suburb?.name != null && !storePickup)
                    itemView.deliverySuburb?.text = item?.orderSummary?.suburb?.name
                if (storePickup)
                    itemView.deliverySuburb?.text = item?.orderSummary?.store?.name
                itemView.deliverySuburbLbl?.visibility =
                    if ((item?.orderSummary?.suburb?.name != null && !storePickup) || storePickup) View.VISIBLE else View.GONE
                itemView.deliverySuburbLbl?.text =
                    context?.resources.getString(if (storePickup) R.string.collection_location else R.string.delivery_suburb)
                itemView.deliverySuburbLbl?.contentDescription =
                    context?.resources.getString(if (storePickup) R.string.collection_location_title else R.string.delivery_suburb)
                itemView.deliverySuburb?.contentDescription =
                    context?.resources.getString(if (storePickup && item.orderSummary.suburb?.name == null) R.string.collection_location_value else R.string.delivery_suburb)
            }else{
                deliveryType = Delivery.getType(delivery)
                when (deliveryType) {
                    Delivery.CNC -> {
                        itemView.deliverySuburbLbl?.text =
                            context?.resources.getString(R.string.collection_location)
                        itemView.deliverySuburb?.text =
                            item.orderSummary?.fulfillmentDetails?.storeName

                        itemView.deliverySuburbLbl?.contentDescription =
                            context?.resources.getString(R.string.collection_location_title)
                        itemView.deliverySuburb?.contentDescription =
                            context?.resources.getString(R.string.delivery_suburb)

                    }
                    Delivery.STANDARD -> {
                        itemView.deliverySuburbLbl?.visibility =
                            if (item.orderSummary?.fulfillmentDetails?.address?.address1.isNullOrEmpty()) View.GONE else View.VISIBLE
                        itemView.deliverySuburbLbl?.text =
                            context?.resources.getString(R.string.delivery_address)
                        itemView.deliverySuburb?.text =
                            item.orderSummary?.fulfillmentDetails?.address?.address1
                    }
                    else -> {
                    }
                }
            }
            if (!item.orderSummary?.deliveryDates.isJsonNull) {
                when (deliveryType) {
                    Delivery.CNC -> {
                        itemView.deliveryItemsType.text =
                            context?.resources.getString(R.string.collection_date)
                        itemView.deliveryItemsType.contentDescription =
                            context?.resources.getString(R.string.collection_details_date_title)
                        itemView.deliveryDate.contentDescription =
                            context?.resources.getString(R.string.collection_details_date_value)
                    }
                    Delivery.STANDARD -> {
                        itemView.deliveryItemsType.text =
                            context?.resources.getString(R.string.delivery_date)
                        itemView.deliveryItemsType.contentDescription =
                            context?.resources.getString(R.string.delivery_location_title1)
                        itemView.deliveryDate.contentDescription =
                            context?.resources.getString(R.string.delivery_location_value)
                    }
                    else -> {
                    }
                }

              itemView.deliveryDateContainer.removeAllViews()
                val deliveryDates: HashMap<String, String> = hashMapOf()
                for (i in 0 until item.orderSummary?.deliveryDates.asJsonArray.size()) {
                    deliveryDates.putAll(Gson().fromJson<Map<String, String>>(item.orderSummary?.deliveryDates.asJsonArray.get(i).toString(), object : TypeToken<Map<String, String>>() {}.type))
                }
                when (deliveryDates.keys.size) {
                    0 -> {
                        itemView.deliveryDateLayout.visibility = View.GONE
                    }
                    1 -> {
                        itemView.deliveryDate.text = deliveryDates.getValue(deliveryDates.keys.toList()[0])
                    }
                    else -> {
                        deliveryDates.entries.forEach { entry ->
                            val view = (context as Activity).layoutInflater.inflate(R.layout.order_deatils_delivery_date_item, null)
                            val deliveryItemsType = view.findViewById<WTextView>(R.id.deliveryItemsType)
                            val dateOfDelivery = view.findViewById<WTextView>(R.id.dateOfDelivery)
                            deliveryItemsType.text = entry.key
                            dateOfDelivery.text = entry.value
                            itemView.deliveryDateContainer.addView(view)
                        }
                    }
                }
            } else {
                itemView.deliveryDateLayout.visibility = View.GONE
            }
        }

    }

    inner class OrderItemViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as CommerceItem
            setProductImage(itemView.imProductImage, item.commerceItemInfo.externalImageRefV2)
            itemView.itemName.text = item.commerceItemInfo.quantity.toString()+" x "+item.commerceItemInfo.productDisplayName
            itemView.price.text = CurrencyFormatter.formatAmountToRandAndCentWithSpace(item.priceInfo.amount)
            itemView.price.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

            itemView.setOnClickListener { listner.onOpenProductDetail(item) }
        }

    }

    inner class GiftViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as CommerceItem
            with(itemView){
                with(item.commerceItemInfo) {
                    setProductImage(freeGiftImageView, externalImageRefV2)
                    giftItemTextView?.text = "$quantity x $productDisplayName"
                }
            }
        }
    }

    inner class HeaderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val orderItemDetail = dataList[position] as? OrderDetailsItem
            val headerText = "${orderItemDetail?.item}${if (orderItemDetail?.orderItemLength!! > 1) "S" else ""}"
            itemView.header?.text = headerText
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
            image.setImageURI( imgUrl + if (imgUrl.indexOf("?") > 0) "w=" + 48 + "&q=" + 48 else "?w=" + 48 + "&q=" + 48)
        }
    }
}