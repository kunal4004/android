package za.co.woolworths.financial.services.android.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.product_listing_price_layout.view.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.ui.adapters.holder.*
import za.co.woolworths.financial.services.android.util.Utils

class ProductListingAdapter(private val navigator: IProductListing?, private val mProductListItems: List<ProductList>?) : RecyclerView.Adapter<ProductListingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListingViewHolder {
        return when (ProductListingViewType.values()[viewType]) {
            ProductListingViewType.HEADER -> ProductListingViewHolderHeader(parent)
            ProductListingViewType.FOOTER -> ProductListingViewHolderFooter(parent)
            else -> ProductListingViewHolderItems(parent)
        }
    }

    override fun onBindViewHolder(holder: ProductListingViewHolder, position: Int) {
        mProductListItems?.get(position)?.let { productList ->
            when (productList.rowType) {
                ProductListingViewType.HEADER -> (holder as? ProductListingViewHolderHeader)?.setNumberOfItems(productList)
                ProductListingViewType.FOOTER -> (holder as? ProductListingViewHolderFooter)?.loadMoreProductProgressBarVisibility()
                else -> (holder as? ProductListingViewHolderItems)?.let { view ->
                    navigator?.let { view.setProductItem(productList, it, if (position % 2 != 0) mProductListItems.getOrNull(position + 1) else null, if (position % 2 == 0) mProductListItems.getOrNull(position - 1) else null) }
                    view.itemView.imQuickShopAddToCartIcon?.setOnClickListener {
                        if (!productList.quickShopButtonWasTapped) {
                            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPQS_ADD_TO_CART)
                            val fulfilmentTypeId = WoolworthsApplication.getQuickShopDefaultValues()?.foodFulfilmentTypeId
                            val storeId = fulfilmentTypeId?.let { it1 -> ProductListingViewHolderItems.getFulFillmentStoreId(it1) }
                            storeId?.let { id -> navigator?.queryInventoryForStore(id, AddItemToCart(productList.productId, productList.sku, 0), productList) }
                        }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int = mProductListItems?.get(position)?.rowType?.ordinal
            ?: 0

    override fun getItemCount(): Int = mProductListItems?.size ?: 0

    override fun getItemId(position: Int): Long = position.toLong()

    fun resetQuickShopButton() {
        mProductListItems?.forEach { product ->
            product.quickShopButtonWasTapped = false
        }
        notifyDataSetChanged()
    }
}