package za.co.woolworths.financial.services.android.ui.adapters.shop.dash

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.*
import com.awfs.coordination.databinding.ItemLayoutOnDemandCategoryBinding
import com.awfs.coordination.databinding.ItemLayoutProductCarouselBinding
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.models.dto.shop.ProductCatalogue
import za.co.woolworths.financial.services.android.ui.views.shop.dash.OnDashLandingNavigationListener
import za.co.woolworths.financial.services.android.ui.views.shop.dash.OnDataUpdateListener
import za.co.woolworths.financial.services.android.ui.views.shop.dash.OnDemandNavigationListener
import za.co.woolworths.financial.services.android.util.ImageManager

class DashDeliveryAdapter(
    @NonNull val context: Context,
    val onDemandNavigationListener: OnDemandNavigationListener,
    val dashLandingNavigationListener: OnDashLandingNavigationListener,
    val onDataUpdateListener: OnDataUpdateListener? = null,
    val iProductListing: IProductListing
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_NAME_BANNER_CAROUSEL = "Banner Carousel"
        const val TYPE_NAME_BANNER_GRID = "Banner Grid"
        const val TYPE_NAME_PRODUCT_CAROUSEL = "Product Carousel"
        const val TYPE_NAME_LONG_BANNER_CAROUSEL = "Long Banners Carousel"
        const val TYPE_NAME_LONG_BANNER_LIST = "Long Banners List"
        const val TYPE_NAME_BANNER_FULL_WIDTH = "Banner FullWidth"
        const val TYPE_NAME_LONG_BANNER_FULL_WIDTH = "Banner FullWidth"

        const val TYPE_EMPTY = 0
        const val TYPE_ON_DEMAND_CATEGORIES = 1
        const val TYPE_DASH_CATEGORIES_BANNER_CAROUSEL = 2
        const val TYPE_DASH_CATEGORIES_BANNER_GRID = 3
        const val TYPE_DASH_CATEGORIES_PRODUCT_CAROUSEL = 4
        const val TYPE_DASH_CATEGORIES_LONG_BANNER_CAROUSEL = 5
        const val TYPE_DASH_CATEGORIES_LONG_BANNER_LIST = 6
        const val TYPE_DASH_TODAY_WITH_WOOLIES = 7

    }

    private val diffCallback = object : DiffUtil.ItemCallback<Any?>() {

        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is ProductCatalogue && newItem is ProductCatalogue -> {
                    oldItem.headerText == newItem.headerText
                }
                oldItem is List<*> && newItem is List<*> -> {
                    true
                }
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is ProductCatalogue && newItem is ProductCatalogue -> {
                    (oldItem as ProductCatalogue).hashCode() == (newItem as ProductCatalogue).hashCode()
                }
                oldItem is List<*> && newItem is List<*> -> {
                    (oldItem as List<RootCategory>).hashCode() == (newItem as List<RootCategory>).hashCode()
                }
                else -> false
            }
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    private var categoryList: List<Any?>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ON_DEMAND_CATEGORIES -> {
                OnDemandCategoryLayoutViewHolder(
                    ItemLayoutOnDemandCategoryBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_DASH_CATEGORIES_BANNER_CAROUSEL -> {
                BannerCarouselLayoutViewHolder(
                    ItemLayoutProductCarouselBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_DASH_CATEGORIES_BANNER_GRID -> {
                BannerGridLayoutViewHolder(
                    ItemLayoutProductCarouselBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_DASH_CATEGORIES_PRODUCT_CAROUSEL -> {
                ProductCarouselLayoutViewHolder(
                    ItemLayoutProductCarouselBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_DASH_CATEGORIES_LONG_BANNER_CAROUSEL -> {
                LongBannerCarouselLayoutViewHolder(
                    ItemLayoutProductCarouselBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_DASH_CATEGORIES_LONG_BANNER_LIST -> {
                LongBannerListLayoutViewHolder(
                    ItemLayoutProductCarouselBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            TYPE_DASH_TODAY_WITH_WOOLIES -> {
                TodayWooliesLayoutViewHolder(
                    ItemLayoutProductCarouselBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false)
                )
            }

            else -> EmptyViewHolder(View(context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is OnDemandCategoryLayoutViewHolder -> {
                holder.bindView(
                    context,
                    position,
                    categoryList[position] as List<RootCategory>,
                    onDemandNavigationListener
                )
            }
            is BannerCarouselLayoutViewHolder -> {
                holder.bindView(
                    context,
                    position,
                    categoryList[position] as ProductCatalogue,
                    dashLandingNavigationListener
                )
            }
            is BannerGridLayoutViewHolder -> {
                if(position < categoryList.size && position >= 0)
                holder.bindView(
                    context,
                    position,
                    categoryList[position] as ProductCatalogue,
                    categoryList[position + 1] as ProductCatalogue,
                    dashLandingNavigationListener
                )
            }
            is ProductCarouselLayoutViewHolder -> {
                holder.bindView(
                    context,
                    position,
                    categoryList[position] as ProductCatalogue,
                    iProductListing
                )
            }
            is LongBannerCarouselLayoutViewHolder -> {
                holder.bindView(
                    context,
                    position,
                    categoryList[position] as ProductCatalogue,
                    dashLandingNavigationListener
                )
            }
            is LongBannerListLayoutViewHolder -> {
                holder.bindView(
                    context,
                    position,
                    categoryList[position] as ProductCatalogue,
                    dashLandingNavigationListener
                )
            }
            is  TodayWooliesLayoutViewHolder -> {
                holder.bindView(
                    context,
                    position,
                    categoryList[position] as ProductCatalogue,
                    dashLandingNavigationListener
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (categoryList[position]) {
            is List<*> -> {
                TYPE_ON_DEMAND_CATEGORIES
            }
            is ProductCatalogue -> {
                val productCatalogue = categoryList[position] as ProductCatalogue
                when (productCatalogue.name?.lowercase()) {
                    TYPE_NAME_BANNER_CAROUSEL.lowercase() -> {
                        TYPE_DASH_CATEGORIES_BANNER_CAROUSEL
                    }
                    TYPE_NAME_BANNER_GRID.lowercase() -> {
                        TYPE_DASH_CATEGORIES_BANNER_GRID
                    }
                    TYPE_NAME_PRODUCT_CAROUSEL.lowercase() -> {
                        TYPE_DASH_CATEGORIES_PRODUCT_CAROUSEL
                    }
                    TYPE_NAME_LONG_BANNER_CAROUSEL.lowercase() -> {
                        TYPE_DASH_CATEGORIES_LONG_BANNER_CAROUSEL
                    }
                    TYPE_NAME_LONG_BANNER_LIST.lowercase() -> {
                        TYPE_DASH_CATEGORIES_LONG_BANNER_LIST
                    }
                    TYPE_NAME_LONG_BANNER_FULL_WIDTH.lowercase() -> {
                        TYPE_DASH_TODAY_WITH_WOOLIES
                    }
                    else -> TYPE_EMPTY
                }
            }

            else -> TYPE_EMPTY
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    fun setData(
        onDemandCategories: List<RootCategory>?,
        dashCategories: ArrayList<ProductCatalogue>?
    ) {
        val list = ArrayList<Any?>(0)
        list.apply {
            onDemandCategories?.let {
                if (it.isNotEmpty()) add(it)
            }
            dashCategories?.let {
                addAll(it)
            }
        }
        categoryList = list
        onDataUpdateListener?.onProductCatalogueUpdate(productCatalogues = dashCategories)
    }
}

class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class OnDemandCategoryLayoutViewHolder(val itemBinding: ItemLayoutOnDemandCategoryBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {

    fun bindView(
        context: Context,
        position: Int,
        onDemandCategories: List<RootCategory>?,
        onDemandNavigationListener: OnDemandNavigationListener
    ) {

        onDemandCategories?.let {
            itemBinding.rvOnDemandCategories?.apply {
                val onDemandCategoryAdapter =
                    OnDemandCategoryAdapter(context, onDemandNavigationListener)
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                adapter = onDemandCategoryAdapter
                onDemandCategoryAdapter.setOnDemandCategoryList(it)
            }
        }
    }
}

class BannerCarouselLayoutViewHolder(val itemBinding: ItemLayoutProductCarouselBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {

    fun bindView(
        context: Context,
        position: Int,
        productCatalogue: ProductCatalogue?,
        dashLandingNavigationListener: OnDashLandingNavigationListener
    ) {

        itemBinding.dashCategoryTitle.text = productCatalogue?.headerText
        itemBinding.rvDashCategories?.apply {
            val bannerCarouselAdapter =
                DashCategoryAdapter(context, dashLandingNavigationListener, null)
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = bannerCarouselAdapter
            productCatalogue?.let {
                bannerCarouselAdapter.setData(it)
            }
        }
    }
}

class BannerGridLayoutViewHolder(val itemBinding: ItemLayoutProductCarouselBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {

    fun bindView(
        context: Context,
        position: Int,
        productCatalogue: ProductCatalogue?,
        nextProductCatalogue: ProductCatalogue?,
        dashLandingNavigationListener: OnDashLandingNavigationListener
    ) {
        itemBinding.imgBannerFullWidth.visibility = GONE
        itemBinding.dashCategoryTitle.text = productCatalogue?.headerText
        itemBinding.rvDashCategories?.apply {
            val bannerGridAdapter =
                DashCategoryAdapter(context, dashLandingNavigationListener, null)
            layoutManager = GridLayoutManager(context, 2)
            adapter = bannerGridAdapter
            productCatalogue?.let {
                bannerGridAdapter.setData(it)
            }
        }
        if (!DashDeliveryAdapter.TYPE_NAME_BANNER_FULL_WIDTH
                .equals(nextProductCatalogue?.name, true)
        ) return
        nextProductCatalogue?.banners?.apply {
            if (this.isEmpty()) return
            itemBinding.imgBannerFullWidth.visibility = get(0).externalImageRefV2?.let {
                ImageManager.setPicture(
                    itemBinding.imgBannerFullWidth,
                    it
                )
                View.VISIBLE
            } ?: GONE
        }
    }
}

class ProductCarouselLayoutViewHolder(val itemBinding: ItemLayoutProductCarouselBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {

    fun bindView(
        context: Context,
        position: Int,
        productCatalogue: ProductCatalogue?,
        iProductListing: IProductListing
    ) {
        itemBinding.dashCategoryTitle.text = productCatalogue?.headerText
        itemBinding.rvDashCategories?.apply {
            val productCarouselAdapter = DashCategoryAdapter(context, null, iProductListing)
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = productCarouselAdapter
            productCatalogue?.let {
                productCarouselAdapter.setData(it)
            }
        }
    }
}

class LongBannerCarouselLayoutViewHolder(val itemBinding: ItemLayoutProductCarouselBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {

    fun bindView(
        context: Context,
        position: Int,
        productCatalogue: ProductCatalogue?,
        dashLandingNavigationListener: OnDashLandingNavigationListener
    ) {
        itemBinding.dashCategoryTitle.text = productCatalogue?.headerText
        itemBinding.rvDashCategories?.apply {
            val longBannerCarouselAdapter =
                DashCategoryAdapter(context, dashLandingNavigationListener, null)
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = longBannerCarouselAdapter
            productCatalogue?.let {
                longBannerCarouselAdapter.setData(it)
            }
        }
    }
}

class LongBannerListLayoutViewHolder(val itemBinding: ItemLayoutProductCarouselBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {

    fun bindView(
        context: Context,
        position: Int,
        productCatalogue: ProductCatalogue?,
        dashLandingNavigationListener: OnDashLandingNavigationListener
    ) {
        itemBinding.dashCategoryTitle.text = productCatalogue?.headerText
        itemBinding.rvDashCategories?.apply {
            val longBannerListAdapter =
                DashCategoryAdapter(context, dashLandingNavigationListener, null)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = longBannerListAdapter
            productCatalogue?.let {
                longBannerListAdapter.setData(it)
            }
        }
    }
}

class TodayWooliesLayoutViewHolder(val itemBinding: ItemLayoutProductCarouselBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {

    fun bindView(
        context: Context,
        position: Int,
        productCatalogue: ProductCatalogue?,
        dashLandingNavigationListener: OnDashLandingNavigationListener,
    ) {
        itemBinding.dashCategoryTitle.text = productCatalogue?.headerText
        itemBinding.rvDashCategories?.apply {
            val longBannerCarouselAdapter =
                DashCategoryAdapter(context, dashLandingNavigationListener, null)
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = longBannerCarouselAdapter
            productCatalogue?.let {
                longBannerCarouselAdapter.setData(it)
            }
        }
    }
}


