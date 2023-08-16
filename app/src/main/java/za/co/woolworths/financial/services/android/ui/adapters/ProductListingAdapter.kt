package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.BottomProgressBarBinding
import com.awfs.coordination.databinding.ItemFoundLayoutBinding
import com.awfs.coordination.databinding.ProductListingPageRowBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.ui.adapters.holder.*
import za.co.woolworths.financial.services.android.util.Utils

class ProductListingAdapter(
    private val navigator: IProductListing?,
    private val mProductListItems: List<ProductList>?,
    val activity: FragmentActivity?,
    val mBannerLabel: String?,
    val mBannerImage: String?,
    val mIsComingFromBLP: Boolean,
    val promotionalRichText: String?,
    val listener:OnTapIcon
) : RecyclerView.Adapter<RecyclerViewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewViewHolder {
        return when (ProductListingViewType.values()[viewType]) {
            ProductListingViewType.HEADER ->
                RecyclerViewViewHolderHeader(
                    ItemFoundLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            ProductListingViewType.FOOTER ->
                RecyclerViewViewHolderFooter(
                    BottomProgressBarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            else ->
                RecyclerViewViewHolderItems(
                    ProductListingPageRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewViewHolder, position: Int) {
        mProductListItems?.get(position)?.let { productList ->
            holder.itemView.invalidate()
            holder.itemView.requestLayout()
            when (productList.rowType) {
                ProductListingViewType.HEADER -> {
                    (holder as? RecyclerViewViewHolderHeader)?.setNumberOfItems(
                        activity, productList
                    )
                    (holder as? RecyclerViewViewHolderHeader)?.setChanelBanner(
                        mBannerLabel, mBannerImage, mIsComingFromBLP, navigator
                    )
                    (holder as? RecyclerViewViewHolderHeader)?.setPromotionalBanner(promotionalRichText
                    )
                }
                ProductListingViewType.FOOTER -> (holder as? RecyclerViewViewHolderFooter)?.loadMoreProductProgressBarVisibility()
                else -> (holder as? RecyclerViewViewHolderItems)?.let { view ->
                    navigator?.let {
                        view.setProductItem(
                            productList,
                            it,
                            if (position % 2 != 0) mProductListItems.getOrNull(position + 1) else null,
                            if (position % 2 == 0) mProductListItems.getOrNull(position - 1) else null
                        )
                    }
                    view.itemBinding.includeProductListingPriceLayout.imQuickShopAddToCartIcon?.setOnClickListener {
                        if (!productList.quickShopButtonWasTapped) {
                            activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPQS_ADD_TO_CART, this) }
                            val fulfilmentTypeId = AppConfigSingleton.quickShopDefaultValues?.foodFulfilmentTypeId
                            val storeId = fulfilmentTypeId?.let { it1 -> RecyclerViewViewHolderItems.getFulFillmentStoreId(it1) }
                            fulfilmentTypeId?.let { id ->
                                navigator?.queryInventoryForStore(
                                    id,
                                    AddItemToCart(productList.productId, productList.sku, 0),
                                    productList
                                )
                            }
                        }
                    }
                    view.itemBinding.imAddToList?.setOnClickListener {
                       listener.onAddToListClicked(productList)
                    }
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerViewViewHolder) {
        if (holder is RecyclerViewViewHolderItems) {
            holder.setIsRecyclable(false)
        }
        super.onViewAttachedToWindow(holder)
    }

    override fun getItemViewType(position: Int): Int =
        mProductListItems?.get(position)?.rowType?.ordinal
            ?: 0

    override fun getItemCount(): Int = mProductListItems?.size ?: 0

    override fun getItemId(position: Int): Long = position.toLong()

    fun resetQuickShopButton() {
        mProductListItems?.forEach { product ->
            product.quickShopButtonWasTapped = false
        }
        notifyDataSetChanged()
    }
    interface OnTapIcon { fun onAddToListClicked(productList: ProductList) }
}