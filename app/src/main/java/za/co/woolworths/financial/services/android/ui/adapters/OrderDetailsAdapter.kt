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
import za.co.woolworths.financial.services.android.common.convertToTitleCase
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.fragments.shop.OrderDetailsFragment
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.wenum.Delivery

class OrderDetailsAdapter(val context: Context, val listner: OnItemClick, var dataList: ArrayList<OrderDetailsItem>) :  RecyclerView.Adapter<OrdersBaseViewHolder>() {
    private var isContainsFood : Boolean = false

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

            itemView?.apply {
                val item = dataList[position].item as OrderDetailsResponse
                item.orderSummary?.let{
                    it.state?.let{
                        orderState?.text = it
                    }
                    it.submittedDate?.let{
                        purchaseDate?.text =
                            WFormatter.formatOrdersDate(it)
                    }
                    it.total?.let{
                        total?.text =
                            CurrencyFormatter.formatAmountToRandAndCentWithSpace(it)
                        total?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    }
                    it.totalItemsCount?.let{
                        noOfItems?.text =
                            it.toString() + if (it > 1) context.getString(
                                R.string.no_of_items
                            ) else context.getString(R.string.no_of_item)
                    }

                    val delivery: String? = it.fulfillmentDetails?.deliveryType
                    var deliveryType: Delivery?
                    if (delivery.isNullOrEmpty()) {
                        val storePickup = it.store != null
                        deliveryType = if (storePickup) Delivery.CNC else Delivery.STANDARD
                        if (it.suburb?.name != null && !storePickup)
                            deliverySuburb?.text = it.suburb?.name
                        if (storePickup)
                            deliverySuburb?.text = it.store?.name
                        deliverySuburbLbl?.visibility =
                            if ((it.suburb?.name != null && !storePickup) || storePickup) View.VISIBLE else View.GONE
                        deliverySuburbLbl?.text =
                            context?.resources?.getString(if (storePickup) R.string.collection_location else R.string.delivery_suburb)
                        deliverySuburbLbl?.contentDescription =
                            context?.resources?.getString(if (storePickup) R.string.collection_location_title else R.string.delivery_suburb)
                        deliverySuburb?.contentDescription =
                            context?.resources?.getString(if (storePickup && it.suburb?.name == null) R.string.collection_location_value else R.string.delivery_suburb)
                    } else {
                        deliveryType = Delivery.getType(delivery)
                        when (deliveryType) {
                            Delivery.CNC -> {
                                deliverySuburbLbl?.text =
                                    context?.resources?.getString(R.string.collection_location)
                                it.fulfillmentDetails.storeName?.let{
                                    deliverySuburb?.text = convertToTitleCase(it)

                                }
                                deliverySuburbLbl?.contentDescription =
                                    context?.resources?.getString(R.string.collection_location_title)
                                deliverySuburb?.contentDescription =
                                    context?.resources?.getString(R.string.delivery_suburb)

                            }
                            Delivery.STANDARD -> {
                                deliverySuburbLbl?.visibility =
                                    if (item.orderSummary?.fulfillmentDetails?.address?.address1.isNullOrEmpty()) View.GONE else View.VISIBLE
                                deliverySuburbLbl?.text =
                                    context?.resources?.getString(R.string.delivery_address)
                                deliverySuburb?.text =
                                    item.orderSummary?.fulfillmentDetails?.address?.address1?.let { convertToTitleCase(it) }
                            }

                        }
                    }
                    if (!item.orderSummary?.deliveryDates.isJsonNull) {
                        when (deliveryType) {
                            Delivery.CNC -> {
                                deliveryItemsType.text =
                                    context?.resources?.getString(R.string.collection_date)
                                deliveryItemsType.contentDescription =
                                    context?.resources?.getString(R.string.collection_details_date_title)
                                deliveryDate.contentDescription =
                                    context?.resources?.getString(R.string.collection_details_date_value)
                            }
                            Delivery.STANDARD -> {
                                deliveryItemsType?.text =
                                    context?.resources?.getString(R.string.delivery_date)
                                deliveryItemsType?.contentDescription =
                                    context?.resources?.getString(R.string.delivery_location_title1)
                                deliveryDate?.contentDescription =
                                    context?.resources?.getString(R.string.delivery_location_value)
                            }

                        }

                        deliveryDateContainer.removeAllViews()
                        val deliveryDates: HashMap<String, String> = hashMapOf()
                        for (i in 0 until item.orderSummary?.deliveryDates.asJsonArray.size()) {
                            deliveryDates.putAll(
                                Gson().fromJson<Map<String, String>>(
                                    item.orderSummary?.deliveryDates.asJsonArray.get(
                                        i
                                    ).toString(), object : TypeToken<Map<String, String>>() {}.type
                                )
                            )
                        }
                        when (deliveryDates.keys.size) {
                            0 -> {
                                deliveryDateLayout?.visibility = View.GONE
                            }
                            1 -> {
                                deliveryDate?.text =
                                    deliveryDates.getValue(deliveryDates.keys.toList()[0])
                            }
                            else -> {
                                deliveryDates.entries.forEach { entry ->
                                    val view = (context as Activity).layoutInflater.inflate(
                                        R.layout.order_deatils_delivery_date_item,
                                        null
                                    )
                                    val deliveryItemsType =
                                        view.findViewById<WTextView>(R.id.deliveryItemsType)
                                    val dateOfDelivery =
                                        view.findViewById<WTextView>(R.id.dateOfDelivery)
                                    deliveryItemsType?.text = entry.key
                                    dateOfDelivery?.text = entry.value
                                    deliveryDateContainer.addView(view)
                                }
                            }
                        }
                    } else {
                        deliveryDateLayout?.visibility = View.GONE
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
                if(isContainsFood && position == dataList.size - 1)
                    promotion_note.visibility = View.VISIBLE
                else
                    promotion_note.visibility = View.GONE
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
            val headerText = "${orderItemDetail?.item}${if (orderItemDetail?.orderItemLength!! > 1) "S" else ""}"
            itemView.header?.text = headerText
            if(headerText.contains(OrderDetailsFragment.PROMO_NOTE_FOOD)) {
                isContainsFood = true
            } else { isContainsFood = false }
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