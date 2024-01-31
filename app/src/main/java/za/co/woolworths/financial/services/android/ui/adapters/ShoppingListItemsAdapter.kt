package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem
import za.co.woolworths.financial.services.android.shoppinglist.component.MyListFlowType
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ProductAvailability
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsNavigator
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.CONST_NO_SIZE
import za.co.woolworths.financial.services.android.util.CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getPreferredDeliveryType
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Utils.getString
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.util.*

/**
 * Created by W7099877 on 2018/03/09.
 */
class ShoppingListItemsAdapter(
    var shoppingListItems: ArrayList<ShoppingListItem>?,
    private val navigator: ShoppingListItemsNavigator
) : RecyclerSwipeAdapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val ITEM_VIEW_TYPE_HEADER = 0
        private const val ITEM_VIEW_TYPE_BASIC = 1
    }

    private var type = getPreferredDeliveryType()
    private var userShouldSetSuburb = userShouldSetSuburb()
    private var mAdapterIsClickable = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> getHeaderViewHolder(parent)
            ITEM_VIEW_TYPE_BASIC -> getSimpleViewHolder(parent)
            else -> getSimpleViewHolder(parent)
        }

    private fun getHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = HeaderViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.search_add_to_list_layout, parent, false
        )
    )

    private fun getSimpleViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ShoppingListItemViewHolder(
            ShoppingListCommerceItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM_VIEW_TYPE_HEADER -> {
                val headerViewHolder = viewHolder as HeaderViewHolder
                headerViewHolder.tvSearchText.setOnClickListener { navigator.onShoppingSearchClick() }
            }

            ITEM_VIEW_TYPE_BASIC -> {
                val holder = viewHolder as? ShoppingListItemViewHolder ?: return
                val shoppingListItem = getItem(position) ?: return

                holder.bind(shoppingListItem)

                when (shoppingListItem.availability) {
                    ProductAvailability.UNAVAILABLE.value -> holder.bindUnavailableProduct()
                    ProductAvailability.OUT_OF_STOCK.value -> holder.bindOutOfStockProduct(
                        shoppingListItem
                    )

                    else -> holder.bindAvailableProduct(shoppingListItem)
                }
            }
        }
    }

    private fun getColorAndSize(color: String?, size: String?): String = buildString {
        append(color ?: "")

        if (size.isNullOrEmpty() || CONST_NO_SIZE.equals(size, ignoreCase = true)) {
            return@buildString
        }
        append(if (!color.isNullOrEmpty()) ", " else "")
        append(size)
    }

    private fun getUnavailableMsgByDeliveryType(context: Context): String {
        val message = getString(context, R.string.unavailable)
        type ?: return message
        return getString(
            context,
            when (type) {
                Delivery.DASH -> R.string.unavailable_with_dash
                Delivery.CNC -> R.string.unavailable_with_collection
                else -> R.string.unavailable
            }
        )
    }

    private fun deleteItemFromList(shoppingListItem: ShoppingListItem, adapterPosition: Int) {
        navigator.onItemDeleteClick(shoppingListItem)
    }

    private fun enableClickEvent(shoppingListItem: ShoppingListItem): Boolean {
        return if (!userShouldSetSuburb()) shoppingListItem.quantityInStock == -1 else false
    }

    private fun createProductList(shoppingListItem: ShoppingListItem): ProductList {
        val productList = ProductList().apply {
            productId = shoppingListItem.productId
            productName = shoppingListItem.displayName
            sku = shoppingListItem.catalogRefId
            externalImageRefV2 = shoppingListItem.externalImageRefV2

            fromPrice = if (shoppingListItem.price.isNullOrEmpty()) "0.0".toFloat()
            else shoppingListItem.price.toFloat()

            val otherSku = OtherSkus().also {
                it.price = shoppingListItem.price.toString()
                it.size = ""
            }
            val otherSkuList: MutableList<OtherSkus> = ArrayList(0)
            otherSkuList.add(otherSku)
            otherSkus = otherSkuList
        }
        return productList
    }

    override fun getItemCount(): Int {
        return shoppingListItems?.size?.plus(1) ?: 1
    }

    val addedItemsCount: Int
        get() {
            var count = 0
            shoppingListItems ?: return count
            shoppingListItems?.map { item ->
                if (item.isSelected)
                    count += item.userQuantity
            }
            return count
        }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

    internal inner class ShoppingListItemViewHolder(val itemBinding: ShoppingListCommerceItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(shoppingListItem: ShoppingListItem) {
            itemBinding?.apply {

                cartProductImage.setImageURI(shoppingListItem.externalImageRefV2)
                tvTitle.text = (shoppingListItem.displayName)
                tvPrice.setText(formatAmountToRandAndCentWithSpace(shoppingListItem.price))
                // Set Color and Size START
                tvColorSize.text =
                    getColorAndSize(shoppingListItem.color, shoppingListItem.size)
                //Swipe Layout
                mItemManger.bindView(root, position)
                swipe.isTopSwipeEnabled = false
                swipe.addDrag(SwipeLayout.DragEdge.Right, swipeRight)

                tvProductAvailability.visibility = GONE
                cbShoppingList.visibility = GONE
                cbShoppingList.isEnabled = false
                llQuantity.visibility = GONE
                rlAddToCart.visibility = if (shoppingListItem.quantityInStock > 0) VISIBLE else GONE
                pbAddIndicator.visibility =
                    if (shoppingListItem.isAddToCartInProgress) VISIBLE else GONE

                // Product Image
                cartProductImage.setOnClickListener {
                    if (!mAdapterIsClickable) return@setOnClickListener
                    val listItem = getItem(position) ?: return@setOnClickListener
                    val productList = createProductList(listItem)
                    navigator.openProductDetailFragment(listItem.displayName, productList)
                }

                // Swipe delete click
                tvDelete.setOnClickListener {
                    if (!mAdapterIsClickable) return@setOnClickListener
                    val item = getItem(position) ?: return@setOnClickListener
                    navigator.onItemDeleteClick(item)
                }
                // Swipe to add click
                tvAddItem.setOnClickListener {
                    if (!mAdapterIsClickable) return@setOnClickListener
                    adapterClickable(false)
                    val item = getItem(position) ?: return@setOnClickListener
                    item.userQuantity = 1
                    item.isSelected = true
                    item.isAddToCartInProgress = true
                    shoppingListItems?.set(position - 1, item)
                    notifyItemChanged(position)
                    pbAddIndicator.visibility = VISIBLE
                    navigator.onItemAddClick(item)
                }
            }
        }

        fun bindUnavailableProduct() {
            itemBinding?.apply {
                iconKebab.visibility = GONE
                adapterClickable(true)
                val msg = getUnavailableMsgByDeliveryType(itemBinding.root.context)
                tvProductAvailability.text = msg
                tvProductAvailability.visibility = VISIBLE
            }
        }

        fun bindOutOfStockProduct(shoppingListItem: ShoppingListItem) {
            itemBinding?.apply {
                iconKebab.visibility = VISIBLE
                adapterClickable(true)
                tvProductAvailability.text =
                    itemBinding.root.context.getString(R.string.out_of_stock)
                tvProductAvailability.visibility = VISIBLE
                iconKebab.setOnClickListener {
                    navigator.naviagteToMoreOptionDialog(shoppingListItem)
                }
            }
        }

        fun bindAvailableProduct(shoppingListItem: ShoppingListItem) {

            itemBinding?.apply {

                if (!shoppingListItem.inventoryCallCompleted) {
                    pbQuantityLoader.visibility = VISIBLE
                    return
                }

                //Inventory progress bar
                pbQuantityLoader.visibility = GONE

                if (shoppingListItem.quantityInStock <= 0) {
                    return
                }

                //enable check box
                cbShoppingList.visibility = VISIBLE
                cbShoppingList.isEnabled = true

                /*
                * shoppingListItem.userShouldSetSuburb - is set to true when user did not cbxSelectShoppingListItem any suburb
                */
                if (userShouldSetSuburb) {
                    adapterClickable(true)
                    return
                }

                cbShoppingList.isChecked = shoppingListItem.isSelected
                tvQuantity.setText(shoppingListItem.userQuantity.toString())
                // Delete button
                val padding = root.context.resources.getDimension(
                    if (shoppingListItem.userQuantity == 1 && shoppingListItem.isSelected)
                        R.dimen.seven_dp else R.dimen.ten_dp
                ).toInt()
                minusDeleteCountImage.setPadding(padding, padding, padding, padding)
                if (shoppingListItem.userQuantity == 1) {
                    if (MyListFlowType.getFlowType() == MyListFlowType.FlowTypeViewOnly) {
                        minusDeleteCountImage.setImageResource(R.drawable.disabled_delete_icon)
                        minusDeleteCountImage.isEnabled = false
                    } else {
                        minusDeleteCountImage.setImageResource(R.drawable.delete_24)
                        minusDeleteCountImage.isEnabled = true
                    }
                } else {
                    minusDeleteCountImage.setImageResource(R.drawable.ic_minus_black)
                    minusDeleteCountImage.isEnabled = true
                }

                // Add button
                addCountImage.visibility =
                    if (shoppingListItem.quantityInStock == 1 ||
                        shoppingListItem.userQuantity == shoppingListItem.quantityInStock
                    ) GONE else VISIBLE

                llQuantity.visibility = if (shoppingListItem.isSelected) VISIBLE else GONE

                cbShoppingList.setOnClickListener {

                    val item = getItem(bindingAdapterPosition) ?: return@setOnClickListener
                    if (enableClickEvent(item) || !mAdapterIsClickable) return@setOnClickListener

                    if (swipe.isSwipeEnabled){
                        swipe.close()
                    }

                    if (!item.isSelected && userShouldSetSuburb()) {
                        item.isSelected = false
                        notifyItemChanged(position, shoppingListItems?.size)
                        navigator.openSetSuburbProcess(item)
                        return@setOnClickListener
                    }
                    if (item.quantityInStock == 0) return@setOnClickListener
                    /*
                     1. By default quantity will be ZERO.
                     2. On Selection it will change to ONE.
                     */
                    item.userQuantity = item.userQuantity.coerceAtLeast(1)
                    item.isSelected = !item.isSelected
                    shoppingListItems?.set(position - 1, item)
                    navigator.onItemSelectionChange(item.isSelected)
                    notifyItemChanged(position, item)
                }


                minusDeleteCountImage.setOnClickListener {

                    val listItem = getItem(position) ?: return@setOnClickListener
                    if (enableClickEvent(listItem) || !mAdapterIsClickable) return@setOnClickListener

                    if (userShouldSetSuburb()) {
                        navigator.openSetSuburbProcess(listItem)
                        return@setOnClickListener
                    }
                    if (listItem.quantityInStock == 0) return@setOnClickListener
                    // One item selected by user in add to list
                    if (listItem.userQuantity == 1) {
                        deleteItemFromList(listItem, position)
                    } else if (listItem.userQuantity > 1) {
                        listItem.userQuantity -= 1
                        shoppingListItems!![position - 1] = listItem
                        navigator.onSubstractListItemCount(listItem)
                        notifyItemChanged(position, listItem)
                    }
                }

                addCountImage.setOnClickListener {

                    val listItem = getItem(position) ?: return@setOnClickListener
                    if (enableClickEvent(listItem) || !mAdapterIsClickable) return@setOnClickListener

                    if (userShouldSetSuburb()) {
                        navigator.openSetSuburbProcess(listItem)
                        return@setOnClickListener
                    }
                    if (listItem.quantityInStock == 0) return@setOnClickListener
                    // One item selected by user in add to list
                    if (listItem.userQuantity < listItem.quantityInStock) {
                        listItem.userQuantity += 1
                        shoppingListItems!![position - 1] = listItem
                        navigator.onAddListItemCount(listItem)
                        notifyItemChanged(position, listItem)
                    }
                }
                iconKebab.visibility = VISIBLE
                if (cbShoppingList.isChecked) {
                    iconKebab.setImageResource(R.drawable.icon_kebab_inactive)
                    iconKebab.isEnabled = false
                } else {
                    iconKebab.setImageResource(R.drawable.icon_kebab_active)
                    iconKebab.isEnabled = true
                }

                iconKebab.setOnClickListener {
                    navigator.naviagteToMoreOptionDialog(shoppingListItem)
                }
            }
        }
    }

    internal inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSearchText: TextView

        init {
            tvSearchText = itemView.findViewById(R.id.textProductSearch)
        }
    }

    @Synchronized
    fun setList(listItems: ArrayList<ShoppingListItem>?) {
        if (listItems.isNullOrEmpty()) {
            return
        }
        type = getPreferredDeliveryType()
        userShouldSetSuburb = userShouldSetSuburb()
        shoppingListItems = listItems
        notifyItemRangeChanged(0, (shoppingListItems?.size ?: 0).plus(1))
        closeAllItems()
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ITEM_VIEW_TYPE_HEADER
            else -> ITEM_VIEW_TYPE_BASIC
        }
    }

    fun adapterClickable(clickable: Boolean) {
        mAdapterIsClickable = clickable
    }

    private fun getItem(position: Int): ShoppingListItem? =
        shoppingListItems?.getOrNull(position - 1)

    fun userShouldSetSuburb(): Boolean = Utils.getPreferredDeliveryLocation() == null

    fun resetSelection() {
        shoppingListItems ?: return
        shoppingListItems?.filter {
            it.isSelected
        }?.map {
            it.userQuantity = 0
            it.isSelected = false
            it.isAddToCartInProgress = false
        }
        notifyItemRangeChanged(0, (shoppingListItems?.size ?: 0).plus(1))
        navigator.onItemSelectionChange(false)
    }

    fun setAddToCartProgress(itemId: String, isProgress: Boolean) {

        shoppingListItems?.filter {
            it.isAddToCartInProgress
        }?.map {
            it.userQuantity = 0
            it.isSelected = false
            it.isAddToCartInProgress = false
        }
        notifyItemRangeChanged(0, (shoppingListItems?.size ?: 0).plus(1))
    }
}