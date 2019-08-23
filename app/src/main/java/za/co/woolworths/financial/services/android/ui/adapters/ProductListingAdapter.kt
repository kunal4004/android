package za.co.woolworths.financial.services.android.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.ui.adapters.holder.*
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridNavigator

class ProductListingAdapter(private val navigator: GridNavigator, private val mProductListItems: List<ProductList>?) : RecyclerView.Adapter<ProductListingViewHolder>() {

    companion object {
        private const val PRODUCT_LIST_HEADER_VIEW_TYPE = 0
        private const val PRODUCT_LIST_ITEM_VIEW_TYPE = 1
        private const val PRODUCT_LIST_FOOTER_VIEW_TYPE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListingViewHolder {
        return when (viewType) {
            PRODUCT_LIST_HEADER_VIEW_TYPE -> ProductListingViewHolderHeader(parent)
            PRODUCT_LIST_FOOTER_VIEW_TYPE -> ProductListingViewHolderFooter(parent)
            else -> ProductListingViewHolderItems(parent)
        }
    }

    override fun onBindViewHolder(holder: ProductListingViewHolder, position: Int) {
        mProductListItems?.get(position)?.apply {
            when {
                viewTypeHeader -> (holder as? ProductListingViewHolderHeader)?.setNumberOfItems(this)
                viewTypeFooter -> (holder as? ProductListingViewHolderFooter)?.loadMoreProductProgressBarVisibility()
                else -> (holder as? ProductListingViewHolderItems)?.setProductItem(this, navigator)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val productList = mProductListItems!![position]
        return when {
            productList.viewTypeHeader -> PRODUCT_LIST_HEADER_VIEW_TYPE
            productList.viewTypeFooter -> PRODUCT_LIST_FOOTER_VIEW_TYPE
            else -> PRODUCT_LIST_ITEM_VIEW_TYPE
        }
    }

    override fun getItemCount(): Int = mProductListItems?.size ?: 0

}