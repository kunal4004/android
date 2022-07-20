package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder
import kotlinx.android.synthetic.main.my_orders_past_orders_header.view.*
import kotlinx.android.synthetic.main.order_details_commerce_item.view.*
import kotlinx.android.synthetic.main.order_details_gift_commerce_item.view.*
import kotlinx.android.synthetic.main.order_history_chat_layout.view.*
import kotlinx.android.synthetic.main.order_history_details_total_amount_layout.view.*
import kotlinx.android.synthetic.main.order_history_type.view.*
import za.co.woolworths.financial.services.android.common.convertToTitleCase
import za.co.woolworths.financial.services.android.models.dto.*
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
                return OrderStatusViewHolder(LayoutInflater.from(context).inflate(R.layout.order_history_type, parent, false))
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
            OrderDetailsItem.ViewType.ORDER_TOTAL.value -> {
                return OrderTotalViewHolder(LayoutInflater.from(context).inflate(R.layout.order_history_details_total_amount_layout, parent, false))
            }
            OrderDetailsItem.ViewType.CHAT_VIEW.value -> {
                return OrderChatViewHolder(LayoutInflater.from(context).inflate(R.layout.order_history_chat_layout, parent, false))
            }
            OrderDetailsItem.ViewType.TRACK_ORDER.value -> {
                return TrackOrderViewHolder(LayoutInflater.from(context).inflate(R.layout.track_order_layout, parent, false))
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

            itemView.apply {
                orderTotalView?.visibility = View.GONE
                orderHistoryDetailsView?.visibility = View.VISIBLE
                orderTypeView?.visibility = View.VISIBLE
                val item = dataList[position].item as OrderDetailsResponse
                item.orderSummary?.let {
                    it.state?.let {
                        orderState?.text = it.drop(6)
                        orderState.setBackgroundResource(if (it.equals("Order Cancelled",
                                true)
                        ) R.drawable.order_state_orange_bg else R.drawable.order_state_bg)
                    }
                    it.orderId?.let { it ->
                        orderNumber?.text = context.getString(R.string.order_id,
                            it.replaceFirstChar { it.uppercase() })
                    }
                    it.submittedDate?.let {
                        purchaseDate?.text =
                            WFormatter.formatOrdersHistoryDate(it)
                    }
                    it.totalItemsCount?.let {
                        numberItem?.text =
                            it.toString() + if (it > 1) context.getString(
                                R.string.no_of_items
                            ) else context.getString(R.string.no_of_item)
                    }

                    val delivery: String? = it.fulfillmentDetails?.deliveryType
                    var deliveryType: Delivery?
                    if (delivery.isNullOrEmpty()) {
                        orderTypeView?.visibility = View.GONE
                        val storePickup = it.store != null
                        deliveryType = if (storePickup) Delivery.CNC else Delivery.STANDARD
                        if (it.suburb?.name != null && !storePickup)
                            deliveryAddress?.text = it.suburb?.name
                        if (storePickup)
                            deliveryAddress?.text = it.store?.name
                        deliveryAddressTitle?.visibility =
                            if ((it.suburb?.name != null && !storePickup) || storePickup) View.VISIBLE else View.GONE
                        deliveryAddressTitle?.text =
                            context?.resources?.getString(if (storePickup) R.string.collection_store else R.string.delivery_address)
                        deliveryAddressTitle?.contentDescription =
                            context?.resources?.getString(if (storePickup) R.string.collection_location_title else R.string.delivery_address)
                        deliveryAddress?.contentDescription =
                            context?.resources?.getString(if (storePickup && it.suburb?.name == null) R.string.collection_location_value else R.string.delivery_address)
                    } else {
                        deliveryType = Delivery.getType(delivery)
                        when (deliveryType) {
                            Delivery.CNC -> {
                                deliveryAddressTitle?.text =
                                    context?.resources?.getString(R.string.collection_store)
                                it.fulfillmentDetails?.storeName?.let {
                                    deliveryAddress?.text = convertToTitleCase(it)
                                }
                                deliveryAddressTitle?.contentDescription =
                                    context?.resources?.getString(R.string.collection_location_title)
                                deliveryAddress?.contentDescription =
                                    context?.resources?.getString(R.string.delivery_address)
                                orderType?.text = context.getString(R.string.click_and_collect)

                            }
                            Delivery.STANDARD -> {
                                deliveryAddressTitle?.visibility =
                                    if (item.orderSummary?.fulfillmentDetails?.address?.address1.isNullOrEmpty()) View.GONE else View.VISIBLE
                                deliveryAddressTitle?.text =
                                    context?.resources?.getString(R.string.delivery_address)
                                deliveryAddress?.text =
                                    item.orderSummary?.fulfillmentDetails?.address?.address1
                                orderType?.text = context.getString(R.string.standard_delivery)
                            }
                            Delivery.DASH -> {
                                deliveryAddressTitle?.visibility =
                                    if (item.orderSummary?.fulfillmentDetails?.address?.address1.isNullOrEmpty()) View.GONE else View.VISIBLE
                                deliveryAddressTitle?.text =
                                    context?.resources?.getString(R.string.delivery_address)
                                deliveryAddress?.text =
                                    item.orderSummary?.fulfillmentDetails?.address?.address1?.let { convertToTitleCase(it) }
                                orderType?.text = context.getString(R.string.dash_delivery)
                                val orderStatus = item.orderSummary.orderStatus as? String
                                if (orderStatus.isNullOrEmpty())
                                    orderState?.text = item.orderSummary.state?.drop(6)
                                else
                                    orderState?.text = orderStatus
                            }

                        }
                    }

                    if (!item.orderSummary?.deliveryDates?.isJsonNull!!) {
                        val deliveryDates: HashMap<String, String> = hashMapOf()
                        deliveryDates.clear()
                        itemView.deliveryDateContainer.removeAllViews()
                        for (i in 0 until (item.orderSummary?.deliveryDates?.asJsonArray?.size() ?: 0)) {
                            deliveryDates.putAll(Gson().fromJson<Map<String, String>>(
                                item.orderSummary?.deliveryDates?.asJsonArray?.get(
                                    i).toString(), object : TypeToken<Map<String, String>>() {}.type))
                        }
                        when (deliveryDates.keys.size) {
                            0 -> {
                                itemView.timeslotTitle?.visibility = View.GONE
                            }
                            1 -> {
                                itemView.timeslot?.text =
                                    deliveryDates.getValue(deliveryDates.keys.toList()[0])
                                itemView.timeslot?.visibility = View.VISIBLE
                            }
                            else -> {
                                itemView.timeslotTitle?.visibility = View.GONE
                                itemView.timeslot?.visibility = View.GONE
                                deliveryDates.entries.forEach { entry ->
                                    val view =
                                        (context as Activity).layoutInflater.inflate(R.layout.orders_list_delivery_date_item,
                                            null)
                                    val deliveryItemsType =
                                        view.findViewById<TextView>(R.id.deliveryItemsType)
                                    val dateOfDelivery =
                                        view.findViewById<TextView>(R.id.dateOfDelivery)
                                    deliveryItemsType.text =
                                        context.getString(R.string.time_slot_value, entry.key)
                                    dateOfDelivery.text = entry.value
                                    itemView.deliveryDateContainer.addView(view)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inner class OrderItemViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as CommerceItem

            itemView?.apply {
                setProductImage(imProductImage, item.commerceItemInfo.externalImageRefV2)
                itemName?.text = item?.commerceItemInfo?.quantity?.toString()+" x "+item?.commerceItemInfo?.productDisplayName
                price?.text = CurrencyFormatter.formatAmountToRandAndCentWithSpace(item?.priceInfo?.amount)
                price?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                setOnClickListener { listner.onOpenProductDetail(item) }
            }

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
            val headerText = "${orderItemDetail?.item}${if (orderItemDetail?.orderItemLength!! > 1) "s" else ""}"
            itemView.header?.text = "${orderItemDetail?.orderItemLength} $headerText"
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
    inner class OrderChatViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            itemView.apply {
                val item = dataList[position].item as OrderDetailsResponse
                item.orderSummary?.let {
                    it.shopperName.let {
                      chatShopperName?.text =
                          HtmlCompat.fromHtml(context.getString(R.string.chat_to_your_shopper, it),
                          HtmlCompat.FROM_HTML_MODE_LEGACY)
                    }
                }
            }
            itemView.setOnClickListener {
                listner.onOpenChatScreen()
            }
        }

    }
    inner class TrackOrderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            itemView.setOnClickListener {
                listner.onOpenTrackOrderScreen()
            }
        }
    }

    inner class OrderTotalViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            itemView?.apply {
                val item = dataList[position].item as OrderDetailsResponse
                item.orderSummary?.let {
                    it.total.let {
                        orderHistoryDetailsTotal?.text =
                            CurrencyFormatter.formatAmountToRandAndCentWithSpace(it)
                        orderAmount?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    }
                }
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
        fun onOpenChatScreen()
        fun onOpenTrackOrderScreen()
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