package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.content.Context
import android.text.Spannable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AddItemToShoppinglistLayoutBinding
import com.awfs.coordination.databinding.CancelOrderLayoutBinding
import com.awfs.coordination.databinding.GeneralComposeViewBinding
import com.awfs.coordination.databinding.MyOrdersPastOrdersHeaderBinding
import com.awfs.coordination.databinding.OrderDetailsCommerceItemBinding
import com.awfs.coordination.databinding.OrderDetailsGiftCommerceItemBinding
import com.awfs.coordination.databinding.OrderDetailsViewTaxInvoiceLayoutBinding
import com.awfs.coordination.databinding.OrderHistoryChatLayoutBinding
import com.awfs.coordination.databinding.OrderHistoryDetailsTotalAmountLayoutBinding
import com.awfs.coordination.databinding.OrderHistoryTypeBinding
import com.awfs.coordination.databinding.TrackOrderLayoutBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.BarcodeFormat
import za.co.woolworths.financial.services.android.common.convertToTitleCase
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsItem
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.OrderSummary
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder
import za.co.woolworths.financial.services.android.ui.fragments.shop.OrderDetailsFragment
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.ui.views.order_again.EndlessAisleBarcodeView
import za.co.woolworths.financial.services.android.ui.views.order_again.OrderState
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight8dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color4ABB77
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD85C11
import za.co.woolworths.financial.services.android.ui.wfs.theme.ErrorLabel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppBackground
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.CustomTypefaceSpan
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.wenum.Delivery

class OrderDetailsAdapter(val context: Context, val listner: OnItemClick, var dataList: ArrayList<OrderDetailsItem>) :  RecyclerView.Adapter<OrdersBaseViewHolder>() {
    private var isContainsFood : Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersBaseViewHolder {
        when (viewType) {
            OrderDetailsItem.ViewType.HEADER.value -> {
                return HeaderViewHolder(
                    MyOrdersPastOrdersHeaderBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            OrderDetailsItem.ViewType.ORDER_STATUS.value -> {
                return OrderStatusViewHolder(
                    OrderHistoryTypeBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            OrderDetailsItem.ViewType.ENDLESS_AISLE_BARCODE.value -> {
                return EndlessAisleBarcodeViewHolder(
                    GeneralComposeViewBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            OrderDetailsItem.ViewType.ADD_TO_LIST_LAYOUT.value -> {
                return AddToListViewHolder(
                    AddItemToShoppinglistLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }

            OrderDetailsItem.ViewType.GIFT.value -> {
                return GiftViewHolder(
                    OrderDetailsGiftCommerceItemBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }

            OrderDetailsItem.ViewType.COMMERCE_ITEM.value -> {
                return OrderItemViewHolder(
                    OrderDetailsCommerceItemBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            OrderDetailsItem.ViewType.VIEW_TAX_INVOICE.value -> {
                return ViewTaxInvoiceViewHolder(
                    OrderDetailsViewTaxInvoiceLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            OrderDetailsItem.ViewType.CANCEL_ORDER.value -> {
                return CancelOrderViewHolder(
                    CancelOrderLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            OrderDetailsItem.ViewType.ORDER_TOTAL.value -> {
                return OrderTotalViewHolder(
                    OrderHistoryDetailsTotalAmountLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            OrderDetailsItem.ViewType.CHAT_VIEW.value -> {
                return OrderChatViewHolder(
                    OrderHistoryChatLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            OrderDetailsItem.ViewType.TRACK_ORDER.value -> {
                return TrackOrderViewHolder(
                    TrackOrderLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
        }
        return HeaderViewHolder(
            MyOrdersPastOrdersHeaderBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: OrdersBaseViewHolder, position: Int) {
        holder?.bind(position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class EndlessAisleBarcodeViewHolder(val itemBinding: GeneralComposeViewBinding) :
        OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            val data = dataList[position].item as? OrderSummary
            if (data?.endlessAisleBarcode.isNullOrEmpty()) return
            val bitmap = Utils.encodeAsBitmap(data?.endlessAisleBarcode, BarcodeFormat.CODE_128, 314, 74)
            itemBinding.composeView.setContent {
                data?.endlessAisleBarcode?.let {
                    OneAppTheme {
                        Column {
                            EndlessAisleBarcodeView(
                                it.chunked(4).joinToString(" "),
                                bitmap
                            )
                            SpacerHeight8dp(bgColor = OneAppBackground)
                        }
                    }
                }
            }
        }
    }

    inner class OrderStatusViewHolder(val itemBinding: OrderHistoryTypeBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            itemBinding.apply {
                orderTotalView?.visibility = View.GONE
                orderHistoryDetailsView?.visibility = View.VISIBLE
                orderTypeView?.visibility = View.VISIBLE
                val item = dataList[position].item as OrderDetailsResponse
                item.orderSummary?.let {

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
                    var orderStatus = ""
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
                                orderStatus = item.orderSummary.orderStatus as? String ?: ""
                            }
                            else -> {}
                        }
                    }
                    it.state?.let { state ->
                        var errorLabel = ""
                        val bgColor = when {
                            state.contains(context.getString(R.string.cancelled))  -> {
                                if(it.endlessAisleOrder)
                                    errorLabel = context.getString(R.string.endless_aisle_order_error_label)
                                ErrorLabel
                            }
                            it.endlessAisleOrder && state.contains(context.getString(R.string.status_awaiting_payment)) -> ColorD85C11
                            else -> Color4ABB77
                        }

                        orderState.setContent {
                            OneAppTheme {
                                val background by remember { mutableStateOf(bgColor) }
                                val status by remember { mutableStateOf(orderStatus.ifEmpty { state }) }
                                val error by remember { mutableStateOf(errorLabel) }
                                OrderState(
                                    stringResource(R.string.order_id, it.orderId?.replaceFirstChar { it.uppercase() } ?: ""),
                                    status.replace(context.getString(R.string.order), "").uppercase(),
                                    error,
                                    background
                                )
                            }
                        }
                    }

                    if (!item.orderSummary?.deliveryDates?.isJsonNull!!) {
                        val deliveryDates: HashMap<String, String> = hashMapOf()
                        deliveryDates.clear()
                        itemBinding.deliveryDateContainer.removeAllViews()
                        for (i in 0 until (item.orderSummary?.deliveryDates?.asJsonArray?.size() ?: 0)) {
                            deliveryDates.putAll(Gson().fromJson<Map<String, String>>(
                                item.orderSummary?.deliveryDates?.asJsonArray?.get(
                                    i).toString(), object : TypeToken<Map<String, String>>() {}.type))
                        }
                        when (deliveryDates.keys.size) {
                            0 -> {
                                itemBinding.timeslot?.text = context.getString(R.string.empty)
                            }
                            1 -> {
                                itemBinding.timeslot?.text =
                                    deliveryDates.getValue(deliveryDates.keys.toList()[0])
                                itemBinding.timeslot?.visibility = View.VISIBLE
                            }
                            else -> {
                                itemBinding.timeslotTitle?.visibility = View.GONE
                                itemBinding.timeslot?.visibility = View.GONE
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
                                    itemBinding.deliveryDateContainer.addView(view)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inner class OrderItemViewHolder(val itemBinding: OrderDetailsCommerceItemBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            val item = dataList[position].item as? CommerceItem ?: return
            itemBinding.apply {
                item.commerceItemInfo?.externalImageRefV2?.let {
                    setProductImage(imProductImage, it)
                }
                itemName.text = buildSpannedString {
                    val typeface =
                        ResourcesCompat.getFont(itemBinding.root.context, R.font.opensans_semi_bold)
                    item.commerceItemInfo?.quantity?.let {
                        append(it.toString())
                        setSpan(
                            CustomTypefaceSpan("opensans", typeface),
                            0,
                            it.toString().length ?: 0,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        append(" x ")
                    }

                    item.commerceItemInfo?.productDisplayName?.let {
                        append(it)
                    }


                }
               price?.text = CurrencyFormatter.formatAmountToRandAndCentWithSpace(item.priceInfo?.amount)
                price?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                root.setOnClickListener { listner.onOpenProductDetail(item) }

                promotionNote.visibility = if(isContainsFood && position == dataList.size - 1) View.VISIBLE else View.GONE
            }

        }

    }

    inner class GiftViewHolder(val itemBinding: OrderDetailsGiftCommerceItemBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            val item = dataList[position].item as CommerceItem
            itemBinding.apply {
                with(item.commerceItemInfo) {
                    setProductImage(freeGiftImageView, externalImageRefV2)
                    giftItemTextView?.text = "$quantity x $productDisplayName"
                }
            }
        }
    }

    inner class HeaderViewHolder(val itemBinding: MyOrdersPastOrdersHeaderBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            val orderItemDetail = dataList[position] as? OrderDetailsItem
            val headerText = "${orderItemDetail?.item}${if (orderItemDetail?.orderItemLength!! > 1) "s" else ""}"
            itemBinding.header?.text = "${orderItemDetail?.orderItemLength} $headerText"
            isContainsFood = headerText.contains(OrderDetailsFragment.PROMO_NOTE_FOOD, true)
        }
    }

    inner class AddToListViewHolder(val itemBinding: AddItemToShoppinglistLayoutBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            itemBinding.root.setOnClickListener {
                listner.onAddToList(getCommerceItemList())
            }
        }
    }

    inner class ViewTaxInvoiceViewHolder(val itemBinding: OrderDetailsViewTaxInvoiceLayoutBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {

            itemBinding.root.setOnClickListener {
                listner.onViewTaxInvoice()
            }
        }

    }

    inner class CancelOrderViewHolder(val itemBinding: CancelOrderLayoutBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            itemBinding.root.setOnClickListener {
                listner.onCancelOrder()
            }
        }

    }
    inner class OrderChatViewHolder(val itemBinding: OrderHistoryChatLayoutBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            itemBinding.apply {
                val item = dataList[position].item as OrderDetailsResponse
                item.orderSummary?.let {
                    it.shopperName.let {
                      chatShopperName?.text =
                          HtmlCompat.fromHtml(context.getString(R.string.chat_to_your_shopper, it),
                          HtmlCompat.FROM_HTML_MODE_LEGACY)
                    }
                }
                root.setOnClickListener {
                    listner.onOpenChatScreen(item.orderSummary?.orderId.toString())
                }
            }
        }

    }
    inner class TrackOrderViewHolder(val itemBinding: TrackOrderLayoutBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            val item = dataList[position].item as OrderDetailsResponse
            itemBinding.root.setOnClickListener {
                item.orderSummary?.driverTrackingURL?.let {
                    listner.onOpenTrackOrderScreen(it)
                }
            }
        }
    }

    inner class OrderTotalViewHolder(val itemBinding: OrderHistoryDetailsTotalAmountLayoutBinding) : OrdersBaseViewHolder(itemBinding.root) {
        override fun bind(position: Int) {
            itemBinding.apply {
                val item = dataList[position].item as OrderDetailsResponse
                item.orderSummary?.let {
                    it.total.let { totalAmount ->
                        orderHistoryDetailsTotal?.text =
                            CurrencyFormatter.formatAmountToRandAndCentWithSpace(totalAmount)
                        orderHistoryDetailsTotal?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
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
        fun onOpenChatScreen(orderID: String?)
        fun onOpenTrackOrderScreen(orderTrackingURL:String)
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