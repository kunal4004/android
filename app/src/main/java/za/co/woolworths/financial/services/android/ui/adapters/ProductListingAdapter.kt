package za.co.woolworths.financial.services.android.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.product_listing_page_row.view.*
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.ui.adapters.holder.*
import za.co.woolworths.financial.services.android.util.Utils

class ProductListingAdapter(private val navigator: IProductListing, private val mProductListItems: List<ProductList>?) : RecyclerView.Adapter<ProductListingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListingViewHolder {
        return when (ProductListingViewType.values()[viewType]) {
            ProductListingViewType.HEADER -> ProductListingViewHolderHeader(parent)
            ProductListingViewType.FOOTER -> ProductListingViewHolderFooter(parent)
            else -> ProductListingViewHolderItems(parent)
        }
    }

    override fun onBindViewHolder(holder: ProductListingViewHolder, position: Int) {
        mProductListItems?.get(position)?.apply {
            when (rowType) {
                ProductListingViewType.HEADER -> (holder as? ProductListingViewHolderHeader)?.setNumberOfItems(this)
                ProductListingViewType.FOOTER -> (holder as? ProductListingViewHolderFooter)?.loadMoreProductProgressBarVisibility()
                else -> {
                    (holder as? ProductListingViewHolderItems)?.let { view ->
                        view.setProductItem(this, navigator)
                        view.itemView.imQuickShopAddToCartIcon.setOnClickListener {
                            // Extract the fulfilmentStoreId for the "fulfillmentType" = "01"
                            val fulfillmentType = fulfillmentType ?: "01"
                            val storeId = Utils.retrieveStoreId(fulfillmentType) ?: "3501"
                            val skuId = sku ?: productId
                            navigator.queryInventoryForStore(if (storeId.isEmpty()) "3501" else storeId, AddItemToCart(productId,skuId,0))
                            notifyDataSetChanged()
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
}