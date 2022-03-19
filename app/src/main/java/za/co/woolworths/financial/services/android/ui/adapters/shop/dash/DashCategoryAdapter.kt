package za.co.woolworths.financial.services.android.ui.adapters.shop.dash

import android.content.Context
import android.text.Html
import android.text.TextUtils
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
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.models.dto.shop.Banner
import za.co.woolworths.financial.services.android.models.dto.shop.ProductCatalogue
import za.co.woolworths.financial.services.android.ui.adapters.holder.PriceItem
import za.co.woolworths.financial.services.android.ui.adapters.shop.dash.DashDeliveryAdapter.Companion.TYPE_EMPTY
import za.co.woolworths.financial.services.android.ui.vto.utils.VirtualTryOnUtil
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
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
                ProductCarouselItemViewHolder(
                    LayoutInflater.from(context)
                        .inflate(R.layout.chanel_products_horizontal_item_cell, parent, false)
                )
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
            holder is ProductCarouselItemViewHolder -> {
                holder.bind(
                    context,
                    position,
                    list[position] as ProductList,
                    list as List<ProductList>
                )
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
        productCatalogue.products?.let { list = it }
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

class ProductCarouselItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(context: Context, position: Int, productList: ProductList, list: List<ProductList>) {
        val nextProduct = if (position % 2 != 0) list.getOrNull(position + 1) else null
        val previousProduct = if (position % 2 == 0) list.getOrNull(position - 1) else null
        val navigator = context as? IProductListing
        with(productList) {
            setProductImage(this)
            setPromotionalImage(promotionImages, virtualTryOn)
            setProductName(this)
            setBrandText(this, nextProduct, previousProduct)
            setPromotionalText(this)
            val priceItem = PriceItem()
            priceItem.setPrice(productList, itemView)
            setProductVariant(this)
            quickShopAddToCartSwitch(this)
            navigator?.let { setOnClickListener(it, this) }
        }
    }

    private fun setOnClickListener(navigator: IProductListing, productList: ProductList) {
        itemView.setOnClickListener { navigator.openProductDetailView(productList) }
    }

    private fun setProductName(productList: ProductList?) = with(itemView) {
        tvProductName.maxLines = 3
        tvProductName.minLines = 1
        tvProductName?.text = productList?.productName ?: ""
    }

    private fun setPromotionalText(productList: ProductList?) = with(itemView) {
        if (productList?.promotionsList?.isEmpty() == false) {
            productList?.promotionsList?.forEachIndexed { i, it ->
                var editedPromotionalText: String? = it.promotionalText
                if (it.promotionalText?.contains(":") == true) {
                    val beforeColon: String? = it.promotionalText?.substringBefore(":")
                    val afterColon: String? = it.promotionalText?.substringAfter(":")
                    editedPromotionalText = "<b>" + beforeColon + ":" + "</b>" + afterColon
                }
                when (i) {
                    0 -> {
                        onlinePromotionalTextView1?.visibility = View.VISIBLE
                        val promotionsListCount = productList?.promotionsList?.size ?: 0
                        onlinePromotionalTextView1?.text = Html.fromHtml(editedPromotionalText)
                        if (promotionsListCount == 1) {
                            onlinePromotionalTextView1?.maxLines = 2
                            onlinePromotionalTextView2?.text = ""
                            onlinePromotionalTextView2?.visibility = View.GONE
                        } else
                            onlinePromotionalTextView1?.maxLines = 1
                    }
                    1 -> {
                        onlinePromotionalTextView2?.visibility = View.VISIBLE
                        onlinePromotionalTextView2?.text = Html.fromHtml(editedPromotionalText)
                    }
                }
            }
        } else {
            onlinePromotionalTextView1?.text = ""
            onlinePromotionalTextView2?.text = ""
        }
    }

    private fun setProductVariant(productList: ProductList?) = with(itemView) {
        val productVarientName = productList?.productVariants ?: ""
        if (!TextUtils.isEmpty(productVarientName)) {
            productVariantTextView?.visibility = View.VISIBLE
            productVariantTextView?.text = productVarientName
        } else {
            productVariantTextView?.visibility = View.GONE
            productVariantTextView?.text = ""
        }
    }

    private fun setBrandText(
        productList: ProductList?,
        nextProduct: ProductList?,
        previousProduct: ProductList?
    ) = with(itemView) {
        brandName?.text = productList?.brandText ?: ""
        previousProduct?.let {
            if (productList?.brandText.isNullOrEmpty() && it.brandText.isNullOrEmpty()) {
                brandName?.visibility = View.GONE
            } else {
                brandName?.visibility =
                    if (productList?.brandText.isNullOrEmpty()) View.GONE else View.VISIBLE
                brandNameFakeView?.visibility =
                    if (productList?.brandText.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
        }
        nextProduct?.let {
            if (productList?.brandText.isNullOrEmpty() && it.brandText.isNullOrEmpty()) {
                brandName?.visibility = View.GONE
            } else {
                brandName?.visibility =
                    if (productList?.brandText.isNullOrEmpty()) View.GONE else View.VISIBLE
                brandNameFakeView?.visibility =
                    if (productList?.brandText.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setPromotionalImage(imPromo: PromotionImages?, virtualTryOn: String?) {
        with(itemView) {
            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

            ImageManager.setPictureOverrideWidthHeight(imReducedImage, imPromo?.reduced ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imFreeGiftImage, imPromo?.freeGift ?: "")
            ImageManager.setPictureOverrideWidthHeight(imSave, imPromo?.save ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imReward, imPromo?.wRewards ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imVitality, imPromo?.vitality ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imNewImage, imPromo?.newImage ?: "")
            if (VirtualTryOnUtil.isVtoConfigAvailable()) {
                ImageManager.setPictureWithoutPlaceHolder(imgTryItOn, virtualTryOn ?: "")
            }
        }
    }

    private fun setProductImage(productList: ProductList) {
        val productImageUrl = productList.externalImageRefV2 ?: ""
        ImageManager.setPicture(
            itemView.imProductImage,
            productImageUrl + if (productImageUrl.indexOf("?") > 0) "w=300&q=85" else "?w=300&q=85"
        )
    }

    private fun quickShopAddToCartSwitch(productList: ProductList?) {
        with(itemView) {
            context?.apply {
                productList?.apply {
                    imQuickShopAddToCartIcon?.visibility = if (productType.equals(
                            getString(R.string.food_product_type),
                            ignoreCase = true
                        )
                    ) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
