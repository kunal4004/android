package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ProductAvailability
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListDetailViewModel
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsNavigator
import za.co.woolworths.financial.services.android.ui.views.WTextView
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
                    ProductAvailability.UNAVAILABLE.value -> { holder.bindUnavailableProduct() }
                    ProductAvailability.OUT_OF_STOCK.value -> { holder.bindOutOfStockProduct() }
                    else -> { holder.bindAvailableProduct(shoppingListItem) }
                }
            }
        }
    }

    private fun getColorAndSize(color: String?, size: String?): String = buildString {
        append(color ?: "")
        append(
            if (!color.isNullOrEmpty() && !size.isNullOrEmpty()
                && !CONST_NO_SIZE.equals(size, ignoreCase = true)
            ) ", " else ""
        )
        append(if (size != null && !CONST_NO_SIZE.equals(size, ignoreCase = true)) size else "")
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
        navigator.onItemDeleteClick(
            shoppingListItem.Id,
            shoppingListItem.productId,
            shoppingListItem.catalogRefId,
            true
        )
    }

    private fun enableClickEvent(shoppingListItem: ShoppingListItem): Boolean {
        return if (!userShouldSetSuburb()) shoppingListItem.quantityInStock == -1 else false
    }

    private fun createProductList(shoppingListItem: ShoppingListItem): ProductList {
        val productList = ProductList()
        productList.productId = shoppingListItem.productId
        productList.productName = shoppingListItem.displayName
        productList.fromPrice =
            java.lang.Float.valueOf(if (TextUtils.isEmpty(shoppingListItem.price)) "0.0" else shoppingListItem.price)
        productList.sku = shoppingListItem.catalogRefId
        productList.externalImageRefV2 = shoppingListItem.externalImageRefV2
        val otherSku = OtherSkus()
        otherSku.price = shoppingListItem.price.toString()
        otherSku.size = ""
        val otherSkuList: MutableList<OtherSkus> = ArrayList()
        productList.otherSkus = ArrayList()
        otherSkuList.add(otherSku)
        productList.otherSkus = otherSkuList
        return productList
    }

    override fun getItemCount(): Int {
        return shoppingListItems!!.size + 1
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
            itemBinding.cartProductImage.setImageURI(shoppingListItem.externalImageRefV2)
            itemBinding.tvTitle.setText(shoppingListItem.displayName)
            itemBinding.tvPrice.setText(formatAmountToRandAndCentWithSpace(shoppingListItem.price))
            // Set Color and Size START
            itemBinding.tvColorSize.text =
                getColorAndSize(shoppingListItem.color, shoppingListItem.size)
            //Swipe Layout
            mItemManger.bindView(itemBinding.root, position)
            itemBinding.swipe.isTopSwipeEnabled = false
            itemBinding.swipe.addDrag(SwipeLayout.DragEdge.Right, itemBinding.swipeRight)

            itemBinding.tvProductAvailability.visibility = GONE
            itemBinding.cbShoppingList.visibility = GONE
            itemBinding.cbShoppingList.isEnabled = false
            itemBinding.llQuantity.visibility = GONE

            // Product Image
            itemBinding.cartProductImage.setOnClickListener {
                if (!mAdapterIsClickable) return@setOnClickListener
                val listItem = getItem(position) ?: return@setOnClickListener
                val productList = createProductList(listItem)
                navigator.openProductDetailFragment(listItem.displayName, productList)
            }

            // Item Container
            itemBinding.llItemContainer.setOnClickListener {
                val listItem = getItem(position) ?: return@setOnClickListener
                val isUnavailable = ProductAvailability.UNAVAILABLE.value.equals(listItem.availability, ignoreCase = true)
                if (isUnavailable) navigator.showListBlackToolTip()
            }

            // Swipe delete click
            itemBinding.tvDelete.setOnClickListener {
                if (!mAdapterIsClickable) return@setOnClickListener
                val item = getItem(position) ?: return@setOnClickListener
                navigator.onItemDeleteClick(
                    item.Id,
                    item.productId,
                    item.catalogRefId,
                    true
                )
            }
        }

        fun bindUnavailableProduct() {
            val msg = getUnavailableMsgByDeliveryType(itemBinding.root.context)
            itemBinding.tvProductAvailability.text = msg
            itemBinding.tvProductAvailability.visibility = VISIBLE
        }

        fun bindOutOfStockProduct() {
            itemBinding.tvProductAvailability.text =
                itemBinding.root.context.getString(R.string.out_of_stock)
            itemBinding.tvProductAvailability.visibility = VISIBLE
        }

        fun bindAvailableProduct(shoppingListItem: ShoppingListItem) {

            if (!shoppingListItem.inventoryCallCompleted) {
                itemBinding.pbQuantityLoader.visibility = VISIBLE
                return
            }

            //Inventory progress bar
            itemBinding.pbQuantityLoader.visibility = GONE

            if (shoppingListItem.quantityInStock <= 0) {
                return
            }
            //enable check box
            itemBinding.cbShoppingList.visibility = VISIBLE
            itemBinding.cbShoppingList.isEnabled = true

            /*
            * shoppingListItem.userShouldSetSuburb - is set to true when user did not cbxSelectShoppingListItem any suburb
            */
            if (userShouldSetSuburb) {
                adapterClickable(true)
                return
            }

            itemBinding.cbShoppingList.isChecked = shoppingListItem.isSelected
            itemBinding.tvQuantity.setText(shoppingListItem.userQuantity.toString())
            // Delete button
            val padding = itemBinding.root.context.resources.getDimension(
                if (shoppingListItem.userQuantity == 1 && shoppingListItem.isSelected)
                    R.dimen.seven_dp else R.dimen.ten_dp
            ).toInt()
            itemBinding.minusDeleteCountImage.setPadding(padding, padding, padding, padding)
            itemBinding.minusDeleteCountImage.setImageResource(
                if (shoppingListItem.userQuantity == 1) R.drawable.delete_24 else R.drawable.ic_minus_black
            )
            // Add button
            itemBinding.addCountImage.visibility =
                if (shoppingListItem.quantityInStock == 1 ||
                    shoppingListItem.userQuantity == shoppingListItem.quantityInStock
                ) GONE else VISIBLE

            when (shoppingListItem.isSelected) {
                true -> {
                    itemBinding.llQuantity.visibility = VISIBLE
                }
                false -> {
                    itemBinding.llQuantity.visibility = GONE
                }
            }

            itemBinding.cbShoppingList.setOnClickListener {

                val item = getItem(bindingAdapterPosition) ?: return@setOnClickListener
                if (enableClickEvent(item) || !mAdapterIsClickable) return@setOnClickListener

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

            itemBinding.minusDeleteCountImage.setOnClickListener {

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

            itemBinding.addCountImage.setOnClickListener {

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
        }
    }

    internal inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSearchText: WTextView

        init {
            tvSearchText = itemView.findViewById(R.id.textProductSearch)
        }
    }

    @Synchronized
    fun setList(listItems: ArrayList<ShoppingListItem>?) {
        if (listItems == null || listItems.isEmpty()) {
            return
        }
        type = getPreferredDeliveryType()
        userShouldSetSuburb = userShouldSetSuburb()
        shoppingListItems = listItems
        notifyItemRangeChanged(0, shoppingListItems!!.size)
        closeAllItems()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isPositionHeader(position)) ITEM_VIEW_TYPE_HEADER else ITEM_VIEW_TYPE_BASIC
    }

    fun adapterClickable(clickable: Boolean) {
        mAdapterIsClickable = clickable
    }

    private fun isPositionHeader(position: Int): Boolean {
        return position == 0
    }

    private fun getItem(position: Int): ShoppingListItem? {
        return shoppingListItems?.getOrNull(position - 1)
    }

    fun userShouldSetSuburb(): Boolean {
        return Utils.getPreferredDeliveryLocation() == null
    }

    fun resetSelection() {
        shoppingListItems ?: return
        shoppingListItems?.filter{
            it.isSelected
        }?.forEach {
            it.userQuantity = 0
            it.isSelected = false
        }
        notifyItemRangeChanged(0, shoppingListItems?.size ?: 0)
        navigator.onItemSelectionChange(false)
    }
}