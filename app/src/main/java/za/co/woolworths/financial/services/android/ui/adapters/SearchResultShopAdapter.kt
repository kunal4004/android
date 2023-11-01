package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BottomProgressBarBinding
import com.awfs.coordination.databinding.ItemFoundLayoutBinding
import com.awfs.coordination.databinding.LayoutCartListProductItemBinding
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType
import za.co.woolworths.financial.services.android.ui.adapters.holder.SearchResultPriceItem
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultNavigator
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.CONST_NO_SIZE

class SearchResultShopAdapter(
    private val context: Context, var productList: List<ProductList>?,
    private val mSearchResultNavigator: SearchResultNavigator?
) : RecyclerSwipeAdapter<RecyclerView.ViewHolder>() {

    private var value = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (ProductListingViewType.values()[viewType]) {
            ProductListingViewType.HEADER -> getHeaderViewHolder(parent)
            ProductListingViewType.FOOTER -> getProgressViewHolder(parent)
            ProductListingViewType.PRODUCT -> getSimpleViewHolder(parent)
        }
    }

    private fun getProgressViewHolder(parent: ViewGroup): ProgressViewHolder {
        return ProgressViewHolder(
            BottomProgressBarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    private fun getSimpleViewHolder(parent: ViewGroup): SimpleViewHolder {
        return SimpleViewHolder(
            LayoutCartListProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private fun getHeaderViewHolder(parent: ViewGroup): HeaderViewHolder {
        return HeaderViewHolder(
            ItemFoundLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val productList = productList!![position]
        when (holder) {
            is HeaderViewHolder -> {
                holder.setTotalItem(productList)
            }
            is ProgressViewHolder -> {
                if (!value) {
                    holder.itemBinding.pbFooterProgress.visibility = VISIBLE
                    holder.itemBinding.pbFooterProgress.isIndeterminate = true
                } else holder.itemBinding.pbFooterProgress.visibility = GONE
            }
            is SimpleViewHolder -> {
                holder.setPrice(productList)
                holder.itemBinding?.apply {
                    promotionalTextLayout.visibility = GONE
                    llQuantity.visibility = GONE
                    strikeThroughGroup.visibility = VISIBLE
                    promotionalTextLayout.visibility = GONE

                    cbShoppingList.visibility = VISIBLE
                    cbShoppingList.isChecked = productList.itemWasChecked
                    tvTitle.text = Html.fromHtml(productList.productName)
                    tvQuantity.setText("1")
                    tvColorSize.setText(productList.displayColorSizeText ?: "")
                    swipe.isRightSwipeEnabled = false
                }
                holder.setCartImage(productList)
                holder.showProgressBar(productList.viewIsLoading)
                holder.itemBinding.cbShoppingList.setOnClickListener {
                    /**
                     * Disable clothing type selection when product detail api is loading
                     * food item type can still be selected.
                     */
                    /**
                     * Disable clothing type selection when product detail api is loading
                     * food item type can still be selected.
                     */
                    val productList: ProductList? =
                        this@SearchResultShopAdapter.productList?.getOrNull(holder.adapterPosition)
                            ?: null
                    val productType = productList?.productType ?: ""
                    if (!productType.equals(FOOD_PRODUCT, ignoreCase = true)) {
                        val unlockSelection = !viewIsLoading()
                        holder.itemBinding.cbShoppingList.isChecked = unlockSelection
                        if (unlockSelection) {
                            onCheckItemClick(holder)
                        }
                    } else {
                        onCheckItemClick(holder)
                    }
                }
                holder.itemBinding.llItemContainer.setOnClickListener {
                    if (!viewIsLoading()) {
                        onItemClick(holder)
                    }
                }
                mItemManger.bindView(holder.itemBinding.root, position)
            }
        }
    }

    private fun onCheckItemClick(vh: SimpleViewHolder) {
        val position = vh.adapterPosition
        val selectedProduct = productList!![position]
        // ProductDetails of type clothing or OtherSkus > 0
        if (clothingTypeProduct(selectedProduct)) {
            selectedProduct.viewIsLoading = !selectedProduct.viewIsLoading
            if (selectedProduct.itemWasChecked) selectedProduct.viewIsLoading = false
            mSearchResultNavigator?.onCheckedItem(
                productList,
                selectedProduct,
                selectedProduct.viewIsLoading
            )
            notifyItemChanged(position)
        } else {
            selectedProduct.itemWasChecked = productWasChecked(selectedProduct)
            mSearchResultNavigator?.onFoodTypeChecked(productList, selectedProduct)
            mSearchResultNavigator?.minOneItemSelected(productList)
            notifyItemChanged(position)
        }
    }

    private fun clothingTypeProduct(selectedProduct: ProductList): Boolean {
        return !selectedProduct.productType.equals(FOOD_PRODUCT, ignoreCase = true)
    }

    private fun onItemClick(vh: SimpleViewHolder) {
        val position = vh.adapterPosition
        val selectedProduct = productList!![position]
        // ProductDetails of type clothing or OtherSkus > 0
        if (clothingTypeProduct(selectedProduct)) {
            mSearchResultNavigator!!.onClothingTypeSelect(selectedProduct)
        } else {
            mSearchResultNavigator!!.onFoodTypeSelect(selectedProduct)
        }
    }

    private fun productWasChecked(prodList: ProductList): Boolean {
        return !prodList.itemWasChecked
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

    private inner class SimpleViewHolder(val itemBinding: LayoutCartListProductItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun setCartImage(productItem: ProductList) {
            productItem.externalImageRefV2?.let { url ->
                itemBinding.cartProductImage?.setImageURI(
                    url + (if ((url.indexOf("?") > 0)) "w=" + 85 + "&q=" + 85 else "?w=" + 85 + "&q=" + 85)
                )
            }
        }

        fun setPrice(productItem: ProductList) {
            val priceItem = SearchResultPriceItem()
            priceItem.setPrice(productItem, itemBinding, true)
        }

        fun showProgressBar(visible: Boolean) {
            itemBinding.pbLoadProduct.visibility = if (visible) VISIBLE else GONE
            itemBinding.btnDeleteRow.visibility = if (visible) GONE else VISIBLE
        }
    }

    private inner class ProgressViewHolder(val itemBinding: BottomProgressBarBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

    }

    private inner class HeaderViewHolder(val itemBinding: ItemFoundLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun setTotalItem(productList: ProductList) {
            if (null != productList.numberOfItems) itemBinding.tvNumberOfItem.text =
                productList.numberOfItems.toString()
            if (productList.numberOfItems == 1) {
                itemBinding.tvFoundItem.text = context.getString(R.string.product_item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return productList!![position].rowType.ordinal
    }

    fun refreshAdapter(value: Boolean, tempProductList: List<ProductList>?) {
        this.value = value
        productList = tempProductList
        notifyDataSetChanged()
    }

    fun setCheckedProgressBar(productList: ProductList) {
        if (this.productList != null) {
            for (pList: ProductList in this.productList!!) {
                if (pList === productList) {
                    pList.viewIsLoading = !pList.viewIsLoading
                }
            }
            notifyDataSetChanged()
        }
    }

    fun setSelectedSku(selectedProduct: ProductList, selectedSKU: OtherSkus) {

        productList?.forEachIndexed { index, productList ->
            if (productList === selectedProduct) {
                productList.sku = selectedSKU.sku
                productList.displayColorSizeText = buildColorSizeText(
                    selectedSKU.colour,
                    selectedSKU.size
                )
                notifyItemChanged(index, productList)
                return@forEachIndexed
            }
        }
    }

    private fun buildColorSizeText(colour: String?, size: String?): String = buildString {
        append(colour ?: "")
        if (!colour.isNullOrEmpty() && colour.equals(size, ignoreCase = true)) {
            return@buildString
        }

        if(size.isNullOrEmpty() || CONST_NO_SIZE.equals(size, ignoreCase = true)) {
            return@buildString
        }

        append(if (!colour.isNullOrEmpty()) ", " else "")
        append(size)
    }

    fun onDeselectSKU(selectedProduct: ProductList, selectedSKU: OtherSkus?) {
        if (productList != null) {
            for (pList: ProductList in productList!!) {
                if (pList === selectedProduct) {
                    pList.itemWasChecked = false
                    pList.viewIsLoading = false
                    pList.displayColorSizeText = ""
                    mSearchResultNavigator?.minOneItemSelected(
                        productList
                    )
                    notifyDataSetChanged()
                    return
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (productList == null) 0 else productList!!.size
    }

    fun viewIsLoading(): Boolean {
        if (productList != null) {
            for (pList: ProductList in productList!!) {
                if (pList.viewIsLoading) {
                    return true
                }
            }
        }
        return false
    }

    fun setSelectedProduct(selectedProduct: ProductList) {
        // Ensure that the product list is not null
        if(productList.isNullOrEmpty()) {
            return
        }

        // Find the index of the selected product within the list
        val index = productList!!.indexOf(selectedProduct)
        if (index < 0 || index >= productList!!.size) {
            // Return if the index is out of range
            return
        }

        // Update the selected item's properties
        val selectedItem = productList!!.getOrNull(index)?.apply {
            viewIsLoading = false
            itemWasChecked = true
        }

        // Notify the adapter that the selected item has changed
        notifyItemChanged(index, selectedItem)
    }

    companion object {
        private val FOOD_PRODUCT = "foodProducts"
    }
}