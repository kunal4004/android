package za.co.woolworths.financial.services.android.ui.adapters.shop.dash

import android.content.Context
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ItemBannerCarouselBinding
import com.awfs.coordination.databinding.ItemBannerGridBinding
import com.awfs.coordination.databinding.ItemLongBannerCarouselBinding
import com.awfs.coordination.databinding.ItemLongBannerListBinding
import com.awfs.coordination.databinding.ItemProductCarouselListBinding
import com.awfs.coordination.databinding.TodayWithWooliesListItemBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureAvailable
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.PromotionImages
import za.co.woolworths.financial.services.android.models.dto.shop.Banner
import za.co.woolworths.financial.services.android.models.dto.shop.ProductCatalogue
import za.co.woolworths.financial.services.android.recommendations.presentation.fragment.RecommendationFragment
import za.co.woolworths.financial.services.android.recommendations.presentation.viewmodel.RecommendationViewModel
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.holder.PriceItem
import za.co.woolworths.financial.services.android.ui.adapters.shop.dash.DashDeliveryAdapter.Companion.TYPE_EMPTY
import za.co.woolworths.financial.services.android.ui.views.shop.dash.OnDashLandingNavigationListener
import za.co.woolworths.financial.services.android.ui.vto.utils.VirtualTryOnUtil
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.Utils

class DashCategoryAdapter(
    val context: Context,
    private val dashLandingNavigationListener: OnDashLandingNavigationListener?,
    private val iProductListing: IProductListing?,
    private val recommendationViewModel: RecommendationViewModel?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var headerText: String? = null
    private var type: String? = null
    private var carouselPosition: Int = -1

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
                    ItemBannerCarouselBinding.inflate(LayoutInflater.from(context), parent, false)
                )

            DashDeliveryAdapter.TYPE_DASH_CATEGORIES_BANNER_GRID -> {
                BannerGridItemViewHolder(
                    ItemBannerGridBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }

            DashDeliveryAdapter.TYPE_DASH_CATEGORIES_PRODUCT_CAROUSEL, DashDeliveryAdapter.TYPE_DASH_RECOMMENDATION_SLOT -> {
                ProductCarouselItemViewHolder(
                    ItemProductCarouselListBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }

            DashDeliveryAdapter.TYPE_DASH_CATEGORIES_LONG_BANNER_CAROUSEL -> {
                LongBannerCarouselItemViewHolder(
                    ItemLongBannerCarouselBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }

            DashDeliveryAdapter.TYPE_DASH_CATEGORIES_LONG_BANNER_LIST -> {
                LongBannerListItemViewHolder(
                    ItemLongBannerListBinding.inflate(LayoutInflater.from(context), parent, false)
                )
            }

            DashDeliveryAdapter.TYPE_DASH_TODAY_WITH_WOOLIES -> {
                TodayWooliesListItemViewHolder(
                    TodayWithWooliesListItemBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }

            else -> EmptyViewHolder(View(context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BannerCarouselItemViewHolder -> {
                holder.bind(
                    context,
                    position,
                    list[position] as Banner,
                    headerText = headerText ?: "",
                    dashLandingNavigationListener
                )
            }
            is BannerGridItemViewHolder -> {
                holder.bind(
                    context,
                    position,
                    list[position] as Banner,
                    headerText = headerText ?: "",
                    dashLandingNavigationListener
                )
            }
            is LongBannerCarouselItemViewHolder -> {
                holder.bind(
                    context,
                    position,
                    list[position] as Banner,
                    headerText = headerText ?: "",
                    dashLandingNavigationListener
                )
            }
            is LongBannerListItemViewHolder -> {
                holder.bind(
                    context,
                    position,
                    list[position] as Banner,
                    headerText = headerText ?: "",
                    dashLandingNavigationListener
                )
            }
            is ProductCarouselItemViewHolder -> {
                holder.bind(
                    context,
                    position,
                    list[position] as ProductList,
                    list as List<ProductList>,
                    iProductListing,
                    dashLandingNavigationListener,
                    recommendationViewModel,
                    carouselPosition
                )
            }
            is TodayWooliesListItemViewHolder -> {
                holder.bind(
                    context,
                    position,
                    list[position] as Banner,
                    headerText = headerText ?: "",
                    dashLandingNavigationListener
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
            DashDeliveryAdapter.TYPE_NAME_LONG_BANNER_CAROUSEL.lowercase() -> {
                DashDeliveryAdapter.TYPE_DASH_CATEGORIES_LONG_BANNER_CAROUSEL
            }
            DashDeliveryAdapter.TYPE_NAME_LONG_BANNER_LIST.lowercase() -> {
                DashDeliveryAdapter.TYPE_DASH_CATEGORIES_LONG_BANNER_LIST
            }
            DashDeliveryAdapter.TYPE_NAME_LONG_BANNER_FULL_WIDTH.lowercase() -> {
                DashDeliveryAdapter.TYPE_DASH_TODAY_WITH_WOOLIES
            }
            DashDeliveryAdapter.TYPE_NAME_RECOMMENDATION_SLOT.lowercase() -> {
                DashDeliveryAdapter.TYPE_DASH_RECOMMENDATION_SLOT
            }
            else -> TYPE_EMPTY
        }
    }

    override fun getItemCount() = list.size

    fun setData(productCatalogue: ProductCatalogue, carouselPosition: Int = -1) {
        headerText = productCatalogue.headerText
        type = productCatalogue.name
        this.carouselPosition = carouselPosition
        productCatalogue.banners?.let { list = it }
        productCatalogue.products?.let { list = it }
    }
}

class TodayWooliesListItemViewHolder(val itemBinding: TodayWithWooliesListItemBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(
        context: Context,
        position: Int,
        banner: Banner,
        headerText: String?,
        dashLandingNavigationListener: OnDashLandingNavigationListener?,
    ) {
        itemBinding.root?.setOnClickListener {
            dashLandingNavigationListener?.onDashLandingNavigationClicked(
                position,
                it,
                banner,
                headerText = headerText)

        }
        itemBinding.todayWooliesTitle?.text = banner.subTitleText
        itemBinding.todayWooliesDescription?.text = banner.descriptionText
        setBannerImage(context, banner, itemBinding.fullWidthImage)

    }
}

class BannerCarouselItemViewHolder(val itemBinding: ItemBannerCarouselBinding) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(
        context: Context,
        position: Int,
        banner: Banner,
        headerText: String?,
        dashLandingNavigationListener: OnDashLandingNavigationListener?
    ) {

        itemBinding.dashBannerCarouselContainer?.setOnClickListener {
            dashLandingNavigationListener?.onDashLandingNavigationClicked(
                position,
                view = it,
                banner,
                headerText
                )
        }
        setBannerImage(context,banner,itemBinding.imgCategoryImage)

    }
}

class BannerGridItemViewHolder(val itemBinding: ItemBannerGridBinding) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(
        context: Context,
        position: Int,
        banner: Banner,
        headerText: String?,
        dashLandingNavigationListener: OnDashLandingNavigationListener?
    ) {
        itemBinding.root.setOnClickListener {
            dashLandingNavigationListener?.onDashLandingNavigationClicked(
                position,
                view = it,
                banner,
                headerText = headerText)
        }
        setBannerImage(context,banner,itemBinding.imgCategoryImage)
    }
}

class LongBannerCarouselItemViewHolder(val itemBinding: ItemLongBannerCarouselBinding) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(
        context: Context,
        position: Int,
        banner: Banner,
        headerText: String?,
        dashLandingNavigationListener: OnDashLandingNavigationListener?
    ) {
        itemBinding.includeItemLongBannerList.longBannerListContainer?.setOnClickListener {
            dashLandingNavigationListener?.onDashLandingNavigationClicked(
                position,
                it,
                banner,
                headerText = headerText)
        }
        itemBinding.includeItemLongBannerList.tvLongBannerTitle?.text = banner.displayName
        itemBinding.includeItemLongBannerList.tvLongBannerSubtitle?.text = banner.subTitle
        setBannerImage(context, banner, itemBinding.includeItemLongBannerList.imgLongBannerItem)

    }

}

private fun setBannerImage(
    context: Context,
    banner: Banner,
    image: ImageView
) {
    Glide.with(context)
        .load(banner.externalImageRefV2)
        .format(DecodeFormat.PREFER_ARGB_8888)
        .placeholder(R.drawable.woolworth_logo_icon)
        .transform(
            RoundedCorners(context.resources.getDimensionPixelOffset(R.dimen.seven_dp))
        )
        .dontAnimate()
        .into(image)
}

class LongBannerListItemViewHolder(val itemBinding: ItemLongBannerListBinding) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(
        context: Context,
        position: Int,
        banner: Banner,
        headerText: String?,
        dashLandingNavigationListener: OnDashLandingNavigationListener?
    ) {
        itemBinding.longBannerListContainer?.setOnClickListener {
            dashLandingNavigationListener?.onDashLandingNavigationClicked(
                position,
                it,
                banner,
                headerText = headerText)

        }
        itemBinding.tvLongBannerTitle?.text = banner.displayName
        itemBinding.tvLongBannerSubtitle?.text = banner.subTitle
        setBannerImage(context,banner,itemBinding.imgLongBannerItem)
    }
}

class ProductCarouselItemViewHolder(val itemBinding: ItemProductCarouselListBinding) : RecyclerView.ViewHolder(itemBinding.root) {
    private var productlistingNavigator: IProductListing? = null
    fun bind(
        context: Context,
        position: Int,
        productList: ProductList,
        list: List<ProductList>,
        iProductListing: IProductListing?,
        dashLandingNavigationListener: OnDashLandingNavigationListener?,
        recommendationViewModel: RecommendationViewModel?,
        carouselPosition: Int
    ) {
        val nextProduct = if (position % 2 != 0) list.getOrNull(position + 1) else null
        val previousProduct = if (position % 2 == 0) list.getOrNull(position - 1) else null

        with(productList) {
            setProductImage(this)
            setPromotionalImage(promotionImages, virtualTryOn)
            setBrandText(this, nextProduct, previousProduct)
            setProductName(this)
            setPromotionalText(this)
            val priceItem = PriceItem()
            priceItem.setPrice(productList, itemBinding.rowLayout.includeProductListingPriceLayout)
            setProductVariant(this)
            quickShopAddToCartSwitch(this)
            iProductListing?.let { navigator ->

                productlistingNavigator = navigator
                itemBinding.rowLayout?.let {
                    it.brandName.setOnClickListener { navigator.openProductDetailView(this, position) }
                    it.tvRangeName.setOnClickListener { navigator.openProductDetailView(this, position) }
                    it.tvProductName.setOnClickListener { navigator.openProductDetailView(this, position) }
                    it.mainImgLayout.setOnClickListener { navigator.openProductDetailView(this, position) }
                }
                itemBinding.rowLayout.includeProductListingPriceLayout.imQuickShopAddToCartIcon?.let { quickShopButton ->
                    quickShopButton.setOnClickListener {
                        if (recommendationViewModel?.getQuickShopButtonPressed() == true) {
                            updateMainRecyclerView()
                            return@setOnClickListener
                        }
                        setQuickshopListener(
                            context,
                            navigator,
                            this,
                            this@ProductCarouselItemViewHolder,
                            dashLandingNavigationListener,
                            recommendationViewModel,
                            carouselPosition
                        )
                    }
                }
                }
            }
        }

    private fun setQuickshopListener(
        context: Context,
        navigator: IProductListing?,
        productList: ProductList,
        viewHolder: ProductCarouselItemViewHolder,
        dashLandingNavigationListener: OnDashLandingNavigationListener?,
        recommendationViewModel: RecommendationViewModel?,
        carouselPosition: Int,
    ) {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.SHOPQS_ADD_TO_CART,
            context as? BottomNavigationActivity
        )
        val fulfilmentTypeId =
            AppConfigSingleton.quickShopDefaultValues?.foodFulfilmentTypeId
        fulfilmentTypeId?.let { id ->
            if (productList.sku.isNullOrEmpty()) {
                // This might be the case of th recommendation product where we do not have sku from API so we'll add productId as a sku here
                productList.sku = productList.productId
            }

            val addItemToCartData = if (isEnhanceSubstitutionFeatureAvailable()) {
                AddItemToCart(
                    productList.productId,
                    productList.sku,
                    0,
                    SubstitutionChoice.SHOPPER_CHOICE.name,
                    ""
                )
            } else {
                AddItemToCart(productList.productId, productList.sku, 0)
            }
            dashLandingNavigationListener?.setProductCarousalItemViewHolder(viewHolder)
            recommendationViewModel?.setCarouselPosition(carouselPosition)
            navigator?.queryInventoryForStore(
                id,
                addItemToCartData,
                productList
            )
        }
    }

    private fun updateMainRecyclerView() {
        productlistingNavigator?.updateMainRecyclerView()
    }

    private fun setProductName(productList: ProductList?) = itemBinding.rowLayout.apply {
        tvProductName?.text = productList?.productName ?: ""
    }

    private fun setPromotionalText(productList: ProductList?) = itemBinding.rowLayout.apply {
        onlinePromotionalTextView1?.visibility = View.INVISIBLE
        onlinePromotionalTextView2?.visibility = View.INVISIBLE
        onlinePromotionalTextView1?.text = ""
        onlinePromotionalTextView2?.text = ""

        if (productList?.promotions?.isEmpty() == false) {
            productList.promotions?.forEachIndexed { i, it ->
                var editedPromotionalText: String? = it.promotionalText
                if (it.promotionalText?.contains(":") == true) {
                    val beforeColon: String? = it.promotionalText?.substringBefore(":")
                    val afterColon: String? = it.promotionalText?.substringAfter(":")
                    editedPromotionalText = "<b>$beforeColon:</b>$afterColon"
                }
                when (i) {
                    0 -> {
                        onlinePromotionalTextView1?.visibility = View.VISIBLE
                        onlinePromotionalTextView1?.text = Html.fromHtml(editedPromotionalText)
                    }
                    1 -> {
                        onlinePromotionalTextView2?.visibility = View.VISIBLE
                        onlinePromotionalTextView2?.text = Html.fromHtml(editedPromotionalText)
                    }
                }
            }
        }
    }

    private fun setProductVariant(productList: ProductList?) = itemBinding.rowLayout.apply {
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
    ) = itemBinding.rowLayout.apply {
        brandName?.text = productList?.brandText ?: ""
        brandName?.visibility =
            if (productList?.brandText.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    private fun setPromotionalImage(imPromo: PromotionImages?, virtualTryOn: String?) {
        itemBinding.rowLayout.productListingPromotionalImage.apply {
            itemBinding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

            ImageManager.setPictureOverrideWidthHeight(imReducedImage, imPromo?.reduced ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imFreeGiftImage, imPromo?.freeGift ?: "")
            ImageManager.setPictureOverrideWidthHeight(imSave, imPromo?.save ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imReward, imPromo?.wRewards ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imVitality, imPromo?.vitality ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imNewImage, imPromo?.newImage ?: "")
            ImageManager.setPictureWithoutPlaceHolder(imageWList, imPromo?.wList ?: "")
            if (VirtualTryOnUtil.isVtoConfigAvailable()) {
                ImageManager.setPictureWithoutPlaceHolder(imgTryItOn, virtualTryOn ?: "")
            }
        }
    }

    private fun setProductImage(productList: ProductList) {
        val productImageUrl = productList.externalImageRefV2 ?: ""
        ImageManager.setPicture(
            itemBinding.rowLayout.imProductImage,
            productImageUrl + if (productImageUrl.indexOf("?") > 0) "w=300&q=85" else "?w=300&q=85"
        )
    }

    private fun quickShopAddToCartSwitch(productList: ProductList?) {
        itemBinding.apply {
            root.context?.apply {
                productList?.apply {
                    rowLayout.includeProductListingPriceLayout.imQuickShopAddToCartIcon?.visibility = if (productType.equals(
                            getString(R.string.food_product_type),
                            ignoreCase = true
                        )
                        || productType.equals(
                            RecommendationFragment.ITEM_TYPE_FOOD, ignoreCase = true)) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
