package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BottomProgressBarBinding
import com.awfs.coordination.databinding.ItemFoundLayoutBinding
import com.awfs.coordination.databinding.ShopSearchProductItemBinding
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType
import za.co.woolworths.financial.services.android.ui.adapters.holder.SearchResultPriceItem
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultNavigator

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
            ShopSearchProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                    holder.itemBinding.pbFooterProgress.visibility = View.VISIBLE
                    holder.itemBinding.pbFooterProgress.isIndeterminate = true
                } else holder.itemBinding.pbFooterProgress.visibility = View.GONE
            }
            is SimpleViewHolder -> {
                holder.setPrice(productList)
                holder.setProductName(productList)
                holder.setCartImage(productList)
                holder.setChecked(productList)
                holder.setDefaultQuantity()
                holder.showProgressBar(productList.viewIsLoading)
                holder.disableSwipeToDelete(false)
                holder.setTvColorSize(productList)
                holder.hideDropdownIcon()
                holder.itemBinding.btnDeleteRow.setOnClickListener {
                    /**
                     * Disable clothing type selection when product detail api is loading
                     * food item type can still be selected.
                     */
                    /**
                     * Disable clothing type selection when product detail api is loading
                     * food item type can still be selected.
                     */
                    val productList: ProductList = this@SearchResultShopAdapter.productList!![holder.adapterPosition]
                    val productType = productList.productType
                    if (!productType.equals(FOOD_PRODUCT, ignoreCase = true)) {
                        val unlockSelection = !viewIsLoading()
                        holder.itemBinding.btnDeleteRow.isChecked = unlockSelection
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
        val otherSkuSize = getOtherSkuSize(selectedProduct)
        // ProductDetails of type clothing or OtherSkus > 0
        if (clothingTypeProduct(selectedProduct)) {
            selectedProduct.viewIsLoading = !selectedProduct.viewIsLoading
            if (selectedProduct.itemWasChecked) selectedProduct.viewIsLoading = false
            selectedProduct.itemWasChecked = productWasChecked(selectedProduct)
            mSearchResultNavigator!!.onCheckedItem(
                productList,
                selectedProduct,
                selectedProduct.viewIsLoading
            )
            notifyItemChanged(position)
        } else {
            selectedProduct.itemWasChecked = productWasChecked(selectedProduct)
            mSearchResultNavigator!!.onFoodTypeChecked(productList, selectedProduct)
            mSearchResultNavigator.minOneItemSelected(productList)
            notifyItemChanged(position)
        }
    }

    private fun clothingTypeProduct(selectedProduct: ProductList): Boolean {
        return !selectedProduct.productType.equals(FOOD_PRODUCT, ignoreCase = true)
    }

    private fun onItemClick(vh: SimpleViewHolder) {
        val position = vh.adapterPosition
        val selectedProduct = productList!![position]
        val otherSkuSize = getOtherSkuSize(selectedProduct)
        // ProductDetails of type clothing or OtherSkus > 0
        if (clothingTypeProduct(selectedProduct)) {
            mSearchResultNavigator!!.onClothingTypeSelect(selectedProduct)
        } else {
            mSearchResultNavigator!!.onFoodTypeSelect(selectedProduct)
        }
    }

    private fun getOtherSkuSize(selectedProduct: ProductList): Int {
        val otherSkuList = selectedProduct.otherSkus
        var otherSkuSize = 0
        if (otherSkuList != null) {
            otherSkuSize = otherSkuList.size
        }
        return otherSkuSize
    }

    private fun productWasChecked(prodList: ProductList): Boolean {
        return !prodList.itemWasChecked
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

    private inner class SimpleViewHolder(val itemBinding: ShopSearchProductItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun setDefaultQuantity() {
            itemBinding.tvQuantity.setText("1")
        }

        fun setCartImage(productItem: ProductList) {
            val externalImageRefV2 = productItem.externalImageRefV2
            if (itemBinding.cartProductImage != null && !TextUtils.isEmpty(externalImageRefV2)) itemBinding.cartProductImage.setImageURI(
                externalImageRefV2 + (if ((externalImageRefV2!!.indexOf("?") > 0)) "w=" + 85 + "&q=" + 85 else "?w=" + 85 + "&q=" + 85)
            )
        }

        fun setProductName(productItem: ProductList) {
            itemBinding.tvTitle.text = Html.fromHtml(productItem.productName)
        }

        fun setPrice(productItem: ProductList) {
            val priceItem = SearchResultPriceItem()
            priceItem.setPrice(productItem, itemBinding, true)
        }

        fun setChecked(productList: ProductList) {
            itemBinding.btnDeleteRow.isChecked = productList.itemWasChecked
        }

        fun showProgressBar(visible: Boolean) {
            itemBinding.pbLoadProduct.visibility = if (visible) View.VISIBLE else View.GONE
            itemBinding.btnDeleteRow.visibility = if (visible) View.GONE else View.VISIBLE
        }

        fun disableSwipeToDelete(enable: Boolean) {
            itemBinding.swipe.isRightSwipeEnabled = enable
        }

        fun setTvColorSize(productlist: ProductList) {
            itemBinding.tvColorSize.setText(if (TextUtils.isEmpty(productlist.displayColorSizeText)) "" else productlist.displayColorSizeText)
        }

        fun hideDropdownIcon() {
            itemBinding.imPrice.visibility = View.GONE
        }
    }

    private inner class ProgressViewHolder(val itemBinding: BottomProgressBarBinding) : RecyclerView.ViewHolder(itemBinding.root) {

    }

    private inner class HeaderViewHolder(val itemBinding: ItemFoundLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {

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
        if (productList != null) {
            for (pList: ProductList in productList!!) {
                if (pList === selectedProduct) {
                    pList.sku = selectedSKU.sku
                    val colour =
                        if (TextUtils.isEmpty(selectedSKU.colour)) "" else (selectedSKU.colour)!!
                    val size = if (TextUtils.isEmpty(selectedSKU.size)) "" else (selectedSKU.size)!!
                    val colourSize = TextUtils.isEmpty(colour) || TextUtils.isEmpty(size)
                    pList.displayColorSizeText =
                        if (colourSize) (colour + "" + size) else ("$colour, $size")
                }
            }
            notifyDataSetChanged()
        }
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

    companion object {
        private val FOOD_PRODUCT = "foodProducts"
    }
}