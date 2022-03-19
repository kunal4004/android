package za.co.woolworths.financial.services.android.ui.adapters.shop.dash

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.item_banner_carousel.view.*
import kotlinx.android.synthetic.main.product_listing_page_row.view.*
import kotlinx.android.synthetic.main.product_listing_price_layout.view.*
import kotlinx.android.synthetic.main.product_listing_promotional_images.view.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.models.dto.shop.Banner
import za.co.woolworths.financial.services.android.models.dto.shop.ProductCatalogue
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.holder.RecyclerViewViewHolderItems
import za.co.woolworths.financial.services.android.ui.adapters.shop.dash.DashDeliveryAdapter.Companion.TYPE_EMPTY
import za.co.woolworths.financial.services.android.ui.vto.utils.VirtualTryOnUtil
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.Utils

class DashCategoryAdapter(
    val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var type: String? = null
    private val diffCallback = object : DiffUtil.ItemCallback<Any>() {

        override fun areItemsTheSame(oldItem: Any, newItem: Any) =
            when {
                oldItem is Banner && newItem is Banner ->
                    oldItem.displayName == newItem.displayName
                oldItem is ProductList && newItem is ProductList ->
                    oldItem.productId == newItem.productId
                else -> false
            }

        override fun areContentsTheSame(oldItem: Any, newItem: Any) =
            when {
                oldItem is Banner && newItem is Banner ->
                    (oldItem as Banner).hashCode() == (newItem as Banner).hashCode()
                oldItem is ProductList && newItem is ProductList ->
                    (oldItem as ProductList).hashCode() == (newItem as ProductList).hashCode()
                else -> false
            }
    }
    private val differ = AsyncListDiffer(this, diffCallback)

    private var list: List<Any>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DashDeliveryAdapter.TYPE_DASH_CATEGORIES_BANNER_CAROUSEL ->
                BannerCarouselItemViewHolder(
                    LayoutInflater.from(context)
                        .inflate(R.layout.item_banner_carousel, parent, false)
                )
            DashDeliveryAdapter.TYPE_DASH_CATEGORIES_BANNER_GRID -> {
                BannerGridItemViewHolder(
                    LayoutInflater.from(context)
                        .inflate(R.layout.item_banner_carousel, parent, false)
                )
            }
            DashDeliveryAdapter.TYPE_DASH_CATEGORIES_PRODUCT_CAROUSEL -> {
                RecyclerViewViewHolderItems(parent)
            }
            else -> EmptyViewHolder(View(context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when {
            holder is BannerCarouselItemViewHolder -> {
                holder.bind(context, position, list[position] as Banner)
            }
            holder is BannerGridItemViewHolder -> {
                holder.bind(context, position, list[position] as Banner)
            }
            holder is RecyclerViewViewHolderItems -> {
                val productList = list[position] as ProductList
                val mProductListItems = list as List<ProductList>
                val navigator = context as? IProductListing
                (holder as? RecyclerViewViewHolderItems)?.let { view ->
                    navigator?.let {
                        view.setProductItem(
                            productList,
                            it,
                            if (position % 2 != 0) mProductListItems.getOrNull(position + 1) else null,
                            if (position % 2 == 0) mProductListItems.getOrNull(position - 1) else null
                        )
                    }
                    view.itemView.imQuickShopAddToCartIcon?.setOnClickListener {
                        if (!productList.quickShopButtonWasTapped) {
                            (context as? BottomNavigationActivity)?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPQS_ADD_TO_CART, this) }
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
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (type?.lowercase()) {
            DashDeliveryAdapter.TYPE_NAME_BANNER_CAROUSEL.lowercase() -> {
                DashDeliveryAdapter.TYPE_DASH_CATEGORIES_BANNER_CAROUSEL
            }
            DashDeliveryAdapter.TYPE_NAME_BANNER_GRID.lowercase() -> {
                DashDeliveryAdapter.TYPE_DASH_CATEGORIES_BANNER_GRID
            }
            DashDeliveryAdapter.TYPE_NAME_PRODUCT_CAROUSEL.lowercase() -> {
                DashDeliveryAdapter.TYPE_DASH_CATEGORIES_PRODUCT_CAROUSEL
            }
            else -> TYPE_EMPTY
        }
    }

    override fun getItemCount() = list.size

    fun setData(productCatalogue: ProductCatalogue) {
        type = productCatalogue.name
        productCatalogue.banners?.let { list = it }
        productCatalogue.products?.let{ list = it }
    }

}

class BannerCarouselItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(context: Context, position: Int, banner: Banner) {
        itemView.tvCategoryTitle?.text = banner.displayName

        Glide.with(context)
            .load(banner.externalImageRefV2)
            .format(DecodeFormat.PREFER_ARGB_8888)
            .placeholder(R.drawable.woolworth_logo_icon)
            .transform(
                CenterCrop(),
                RoundedCorners(context.resources.getDimensionPixelOffset(R.dimen.fifteen_dp))
            )
            .dontAnimate()
            .into(itemView.imgCategoryImage)
    }
}

class BannerGridItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(context: Context, position: Int, banner: Banner) {
        // No text
        itemView.tvCategoryTitle?.text = ""

        Glide.with(context)
            .load(banner.externalImageRefV2)
            .format(DecodeFormat.PREFER_ARGB_8888)
            .placeholder(R.drawable.woolworth_logo_icon)
            .transform(
                RoundedCorners(context.resources.getDimensionPixelOffset(R.dimen.fifteen_dp))
            )
            .dontAnimate()
            .into(itemView.imgCategoryImage)
    }
}
