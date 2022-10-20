package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.text.TextUtils.isEmpty
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.MyOrdersPastOrdersHeaderBinding
import com.awfs.coordination.databinding.OrdersToCartCommerceItemBinding
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsItem
import za.co.woolworths.financial.services.android.models.dto.OrderHistoryCommerceItem
import za.co.woolworths.financial.services.android.ui.adapters.holder.OrdersBaseViewHolder
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils

class AddOrderToCartAdapter(val context: Context, val listner: OnItemClick, var dataList: ArrayList<OrderDetailsItem>) : RecyclerView.Adapter<OrdersBaseViewHolder>() {

    private var mAdapterIsClickable: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersBaseViewHolder {
        return when (viewType) {
            OrderDetailsItem.ViewType.HEADER.value -> {
                HeaderViewHolder(
                    MyOrdersPastOrdersHeaderBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            OrderDetailsItem.ViewType.COMMERCE_ITEM.value -> {
                OrderItemViewHolder(
                    OrdersToCartCommerceItemBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }
            else -> HeaderViewHolder(
                MyOrdersPastOrdersHeaderBinding.inflate(LayoutInflater.from(context), parent, false)
            )
        }
    }

    override fun getItemCount(): Int = dataList.size


    override fun onBindViewHolder(holder: OrdersBaseViewHolder, position: Int) {
        holder.bind(position)
    }


    inner class OrderItemViewHolder(val binding: OrdersToCartCommerceItemBinding) : OrdersBaseViewHolder(binding.root) {
        override fun bind(position: Int) {
            val item = dataList[position].item as OrderHistoryCommerceItem
            setProductImage(binding.cartProductImage, item.commerceItemInfo.externalImageRefV2)
            binding.tvTitle.text = item.commerceItemInfo.productDisplayName
            binding.tvQuantity.text = item.userQuantity.toString()
            binding.tvPrice.text = CurrencyFormatter.formatAmountToRandAndCentWithSpace(item.priceInfo.amount)
            binding.selector.isChecked = item.isSelected

            // Set Color and Size START
            var sizeColor: String? = item.color
            if (sizeColor == null)
                sizeColor = ""
            if (sizeColor.isEmpty() && !item.size.isEmpty() && !item.size.equals("NO SZ", ignoreCase = true))
                sizeColor = item.size
            else if (!sizeColor.isEmpty() && !item.size.isEmpty() && !item.size.equals("NO SZ", ignoreCase = true))
                sizeColor = sizeColor + ", " + item.size

            binding.tvColorSize.setText(sizeColor)
            binding.tvColorSize.setVisibility(View.VISIBLE)
            /****
             * item.userShouldSetSuburb - is set to true when user did not select any suburb
             */


            if (userShouldSetSuburb()) {
                binding.tvProductAvailability.setVisibility(View.GONE)
                binding.llQuantity.setVisibility(View.VISIBLE)
                binding.llQuantity.setAlpha(1.0f)
                binding.selector.setEnabled(true)
                adapterClickable(true)
                binding.selector.setAlpha(1.0f)
                binding.llQuantity.setEnabled(true)
            } else {
                if (item != null) {
                    val productInStock = item.quantityInStock != 0
                    binding.llQuantity.setAlpha(if (productInStock) 1.0f else 0.5f)
                    binding.tvQuantity.setAlpha(if (productInStock) 1.0f else 0.5f)
                    binding.selector.setEnabled(productInStock)
                    binding.imPrice.setAlpha(if (productInStock) 1.0f else 0.5f)
                    if (item.inventoryCallCompleted) {
                        val inventoryQueryStatus = item.quantityInStock
                        if (inventoryQueryStatus == -1) {
                            binding.llQuantity.setVisibility(View.GONE)
                            binding.selector.setVisibility(View.GONE)
                            binding.imPrice.setAlpha(0.5f)
                            binding.tvColorSize.setVisibility(View.GONE)
                            binding.tvProductAvailability.setVisibility(View.VISIBLE)
                            binding.tvPrice.setAlpha(0f)
                            binding.tvPrice.setVisibility(View.GONE)
                            Utils.setBackgroundColor(binding.tvProductAvailability, R.drawable.round_amber_corner, R.string.out_of_stock)
                        } else {
                            binding.llQuantity.setVisibility(if (item.quantityInStock == 0) View.GONE else View.VISIBLE)
                            binding.tvProductAvailability.setVisibility(if (item.quantityInStock == 0) View.VISIBLE else View.GONE)
                            binding.selector.setVisibility(if (item.quantityInStock == 0) View.GONE else View.VISIBLE)
                            binding.tvPrice.setVisibility(if (item.quantityInStock == 0) View.GONE else View.VISIBLE)
                            binding.tvPrice.setAlpha(1f)
                            binding.tvColorSize.setVisibility(View.VISIBLE)
                            Utils.setBackgroundColor(binding.tvProductAvailability, R.drawable.round_amber_corner, R.string.out_of_stock)
                        }
                    } else {
                        binding.llQuantity.setVisibility(View.VISIBLE)
                        binding.tvProductAvailability.setVisibility(View.GONE)
                    }
                }
            }



            if (!userShouldSetSuburb())
                if (!item.inventoryCallCompleted) {
                    binding.llQuantity.setAlpha(0.5f)
                    binding.tvQuantity.setAlpha(0.5f)
                    binding.imPrice.setAlpha(0.5f)
                }

            // Set Color and Size END
            binding.selector.setOnClickListener(View.OnClickListener {

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


            binding.llQuantity.setOnClickListener(View.OnClickListener {
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

    inner class HeaderViewHolder(val binding: MyOrdersPastOrdersHeaderBinding) : OrdersBaseViewHolder(binding.root) {
        override fun bind(position: Int) {
            val orderDetailsItem  = dataList[position] as? OrderDetailsItem
            val headerText  = "${orderDetailsItem?.item}${if (orderDetailsItem?.orderItemLength!! > 1) "S" else "" }"
            binding.header?.text =headerText
        }

    }


    override fun getItemViewType(position: Int): Int {
        return dataList[position].type.value
    }

    fun userShouldSetSuburb(): Boolean {
        return Utils.getPreferredDeliveryLocation() == null
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
            image.setImageURI( imgUrl + if (imgUrl.indexOf("?") > 0) "w=" + 85 + "&q=" + 85 else "?w=" + 85 + "&q=" + 85)
        }
    }
}
