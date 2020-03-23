package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils.isEmpty
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_orders_past_orders_header.view.*
import kotlinx.android.synthetic.main.orders_to_cart_commerce_item.view.*
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsItem
import za.co.woolworths.financial.services.android.models.dto.OrderHistoryCommerceItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

class AddOrderToCartAdapter(val context: Context, val listner: OnItemClick, var dataList: ArrayList<OrderDetailsItem>) : RecyclerView.Adapter<OrdersBaseViewHolder>() {

    private var mAdapterIsClickable: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersBaseViewHolder {
        return when (viewType) {
            OrderDetailsItem.ViewType.HEADER.value -> {
                HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_orders_header, parent, false))
            }
            OrderDetailsItem.ViewType.COMMERCE_ITEM.value -> {
                OrderItemViewHolder(LayoutInflater.from(context).inflate(R.layout.orders_to_cart_commerce_item, parent, false))
            }
            else -> HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.my_orders_past_orders_header, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: OrdersBaseViewHolder, position: Int) {
        holder.bind(position)
    }


    inner class OrderItemViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as OrderHistoryCommerceItem
            setProductImage(itemView.cartProductImage, item.commerceItemInfo.externalImageURL)
            itemView.tvTitle.text = item.commerceItemInfo.productDisplayName
            itemView.tvQuantity.text = item.userQuantity.toString()
            itemView.tvPrice.text = WFormatter.formatAmount(item.priceInfo.amount)
            itemView.selector.isChecked = item.isSelected

            // Set Color and Size START
            var sizeColor: String? = item.color
            if (sizeColor == null)
                sizeColor = ""
            if (sizeColor.isEmpty() && !item.size.isEmpty() && !item.size.equals("NO SZ", ignoreCase = true))
                sizeColor = item.size
            else if (!sizeColor.isEmpty() && !item.size.isEmpty() && !item.size.equals("NO SZ", ignoreCase = true))
                sizeColor = sizeColor + ", " + item.size

            itemView.tvColorSize.setText(sizeColor)
            itemView.tvColorSize.setVisibility(View.VISIBLE)
            /****
             * item.userShouldSetSuburb - is set to true when user did not select any suburb
             */


            if (userShouldSetSuburb()) {
                itemView.tvProductAvailability.setVisibility(View.GONE)
                itemView.llQuantity.setVisibility(View.VISIBLE)
                itemView.llQuantity.setAlpha(1.0f)
                itemView.selector.setEnabled(true)
                adapterClickable(true)
                itemView.selector.setAlpha(1.0f)
                itemView.llQuantity.setEnabled(true)
            } else {
                if (item != null) {
                    val productInStock = item.quantityInStock != 0
                    itemView.llQuantity.setAlpha(if (productInStock) 1.0f else 0.5f)
                    itemView.tvQuantity.setAlpha(if (productInStock) 1.0f else 0.5f)
                    itemView.selector.setEnabled(productInStock)
                    itemView.imPrice.setAlpha(if (productInStock) 1.0f else 0.5f)
                    if (item.inventoryCallCompleted) {
                        val inventoryQueryStatus = item.quantityInStock
                        if (inventoryQueryStatus == -1) {
                            itemView.llQuantity.setVisibility(View.GONE)
                            itemView.selector.setVisibility(View.GONE)
                            itemView.imPrice.setAlpha(0.5f)
                            itemView.tvColorSize.setVisibility(View.GONE)
                            itemView.tvProductAvailability.setVisibility(View.VISIBLE)
                            itemView.tvPrice.setAlpha(0f)
                            itemView.tvPrice.setVisibility(View.GONE)
                            Utils.setBackgroundColor(itemView.tvProductAvailability, R.drawable.round_amber_corner, R.string.out_of_stock)
                        } else {
                            itemView.llQuantity.setVisibility(if (item.quantityInStock == 0) View.GONE else View.VISIBLE)
                            itemView.tvProductAvailability.setVisibility(if (item.quantityInStock == 0) View.VISIBLE else View.GONE)
                            itemView.selector.setVisibility(if (item.quantityInStock == 0) View.GONE else View.VISIBLE)
                            itemView.tvPrice.setVisibility(if (item.quantityInStock == 0) View.GONE else View.VISIBLE)
                            itemView.tvPrice.setAlpha(1f)
                            itemView.tvColorSize.setVisibility(View.VISIBLE)
                            Utils.setBackgroundColor(itemView.tvProductAvailability, R.drawable.round_amber_corner, R.string.out_of_stock)
                        }
                    } else {
                        itemView.llQuantity.setVisibility(View.VISIBLE)
                        itemView.tvProductAvailability.setVisibility(View.GONE)
                    }
                }
            }



            if (!userShouldSetSuburb())
                if (!item.inventoryCallCompleted) {
                    itemView.llQuantity.setAlpha(0.5f)
                    itemView.tvQuantity.setAlpha(0.5f)
                    itemView.imPrice.setAlpha(0.5f)
                }

            // Set Color and Size END
            itemView.selector.setOnClickListener(View.OnClickListener {

                if (enableClickEvent(item)) return@OnClickListener
                if (!mAdapterIsClickable) return@OnClickListener
                if (!item.isSelected) {
                    if (userShouldSetSuburb()) {
                        item.isSelected = false
                        notifyDataSetChanged()
                        listner.openSetSuburbProcess()
                        return@OnClickListener
                    }
                }
                if (item.quantityInStock == 0) return@OnClickListener
                /*
                                 1. By default quantity will be ZERO.
                                 2. On Selection it will change to ONE.
                                 */
                item.userQuantity = if (item.isSelected) 0 else 1
                item.isSelected = !item.isSelected
                notifyDataSetChanged()
                listner.onItemSelectionChanged(dataList)
            })


            itemView.llQuantity.setOnClickListener(View.OnClickListener {
                if (enableClickEvent(item)) return@OnClickListener
                if (!mAdapterIsClickable) return@OnClickListener
                if (userShouldSetSuburb()) {
                    listner.openSetSuburbProcess()
                    return@OnClickListener
                }
                if (item.quantityInStock == 0) return@OnClickListener

                if (item.quantityInStock == 0) return@OnClickListener

                listner.onQuantityUpdate(position, item)
            })
        }

    }

    inner class HeaderViewHolder(itemView: View) : OrdersBaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val item = dataList[position].item as String
            itemView.header.text = item
        }

    }


    override fun getItemViewType(position: Int): Int {
        return dataList[position].type.value
    }

    fun userShouldSetSuburb(): Boolean {
        val deliveryLocation = Utils.getPreferredDeliveryLocation() ?: return true
        return deliveryLocation.suburb == null
    }

    public fun adapterClickable(clickable: Boolean) {
        this.mAdapterIsClickable = clickable
    }

    private fun enableClickEvent(orderHistoryCommerceItem: OrderHistoryCommerceItem): Boolean {
        if (!userShouldSetSuburb())
            if (orderHistoryCommerceItem.quantityInStock == -1) return true
        return false
    }

    interface OnItemClick {
        fun onItemSelectionChanged(dataList: ArrayList<OrderDetailsItem>)
        fun onQuantityUpdate(position: Int, quantityInStock: OrderHistoryCommerceItem)
        fun openSetSuburbProcess()
    }

    fun getListItems(): ArrayList<OrderDetailsItem> {
        return dataList
    }

    fun updateList(updatedListItems: ArrayList<OrderDetailsItem>?) {
        /***
         * Update old list with new list before refreshing the adapter
         */
        if (updatedListItems == null) return
        if (dataList == null) return
        try {
            for (dataListItem in dataList) {
                if (dataListItem.type == OrderDetailsItem.ViewType.COMMERCE_ITEM) {
                    for (updatedListItem in updatedListItems) {
                        if (updatedListItem.type == OrderDetailsItem.ViewType.COMMERCE_ITEM) {
                            var item = dataListItem.item as OrderHistoryCommerceItem
                            var updatedItem = dataListItem.item as OrderHistoryCommerceItem
                            if (item.commerceItemInfo.catalogRefId.equals(updatedItem.commerceItemInfo.catalogRefId, ignoreCase = true)) {
                                updatedItem.inventoryCallCompleted = item.inventoryCallCompleted
                                updatedItem.userQuantity = item.userQuantity
                                updatedItem.quantityInStock = item.quantityInStock
                                updatedItem.delivery_location = item.delivery_location
                                updatedItem.isSelected = item.isSelected
                            }
                        }
                    }
                }

            }
            this.dataList = updatedListItems
            this.listner.onItemSelectionChanged(dataList)
            notifyDataSetChanged()
        } catch (ex: IllegalArgumentException) {
            Log.e("updateList", ex.toString())
        }

    }

    private fun setProductImage(image: WrapContentDraweeView, imgUrl: String) {
        if (!isEmpty(imgUrl)) {
            image.setResizeImage(true)
            image.setImageURI(Utils.getExternalImageRef() + imgUrl + if (imgUrl.indexOf("?") > 0) "w=" + 85 + "&q=" + 85 else "?w=" + 85 + "&q=" + 85)
        }
    }
}
