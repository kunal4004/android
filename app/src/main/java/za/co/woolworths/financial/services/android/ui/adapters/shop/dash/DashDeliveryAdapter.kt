package za.co.woolworths.financial.services.android.ui.adapters.shop.dash

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.*
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.item_layout_on_demand_category.view.*
import kotlinx.android.synthetic.main.item_layout_product_carousel.view.*
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.models.dto.shop.ProductCatalogue
import za.co.woolworths.financial.services.android.ui.views.shop.dash.OnDashLandingNavigationListener
import za.co.woolworths.financial.services.android.ui.views.shop.dash.OnDemandNavigationListener
import java.util.*

class DashDeliveryAdapter(
    @NonNull val context: Context,
    val onDemandNavigationListener: OnDemandNavigationListener,
    val dashLandingNavigationListener: OnDashLandingNavigationListener,
    val iProductListing: IProductListing
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_NAME_BANNER_CAROUSEL = "Banner Carousel"
        const val TYPE_NAME_BANNER_GRID = "Banner Grid"
        const val TYPE_NAME_PRODUCT_CAROUSEL = "Product Carousel"
        const val TYPE_NAME_LONG_BANNER_CAROUSEL = "Long Banners Carousel"
        const val TYPE_NAME_LONG_BANNER_LIST = "Long Banners List"

        const val TYPE_EMPTY = 0
        const val TYPE_ON_DEMAND_CATEGORIES = 1
        const val TYPE_DASH_CATEGORIES_BANNER_CAROUSEL = 2
        const val TYPE_DASH_CATEGORIES_BANNER_GRID = 3
        const val TYPE_DASH_CATEGORIES_PRODUCT_CAROUSEL = 4
        const val TYPE_DASH_CATEGORIES_LONG_BANNER_CAROUSEL = 5
        const val TYPE_DASH_CATEGORIES_LONG_BANNER_LIST = 6
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
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_layout_on_demand_category,
                        parent,
                        false
                    )
                )
            }
            TYPE_DASH_CATEGORIES_BANNER_CAROUSEL -> {
                BannerCarouselLayoutViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_layout_product_carousel,
                        parent,
                        false
                    )
                )
            }
            TYPE_DASH_CATEGORIES_BANNER_GRID -> {
                BannerGridLayoutViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_layout_product_carousel,
                        parent,
                        false
                    )
                )
            }
            TYPE_DASH_CATEGORIES_PRODUCT_CAROUSEL -> {
                ProductCarouselLayoutViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_layout_product_carousel,
                        parent,
                        false
                    )
                )
            }
            TYPE_DASH_CATEGORIES_LONG_BANNER_CAROUSEL -> {
                LongBannerCarouselLayoutViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_layout_product_carousel,
                        parent,
                        false
                    )
                )
            }
            TYPE_DASH_CATEGORIES_LONG_BANNER_LIST -> {
                LongBannerListLayoutViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_layout_product_carousel,
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
            is OnDemandCategoryLayoutViewHolder -> {
                holder.bindView(
                    context,
                    position,
                    categoryList[position] as List<RootCategory>,
                    onDemandNavigationListener
                )
            }
            is BannerCarouselLayoutViewHolder -> {
                holder.bindView(context, position, categoryList[position] as ProductCatalogue, dashLandingNavigationListener)
            }
            is BannerGridLayoutViewHolder -> {
                holder.bindView(context, position, categoryList[position] as ProductCatalogue, dashLandingNavigationListener)
            }
            is ProductCarouselLayoutViewHolder -> {
                holder.bindView(context, position, categoryList[position] as ProductCatalogue, iProductListing)
            }
            is LongBannerCarouselLayoutViewHolder -> {
                holder.bindView(context, position, categoryList[position] as ProductCatalogue, dashLandingNavigationListener)
            }
            is LongBannerListLayoutViewHolder -> {
                holder.bindView(context, position, categoryList[position] as ProductCatalogue, dashLandingNavigationListener)
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
    }
}

class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class OnDemandCategoryLayoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindView(
        context: Context,
        position: Int,
        onDemandCategories: List<RootCategory>?,
        onDemandNavigationListener: OnDemandNavigationListener
    ) {

        onDemandCategories?.let {
            itemView.rvOnDemandCategories?.apply {
                val onDemandCategoryAdapter = OnDemandCategoryAdapter(context, onDemandNavigationListener)
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                adapter = onDemandCategoryAdapter
                onDemandCategoryAdapter.setOnDemandCategoryList(it)
            }
        }
    }
}

class BannerCarouselLayoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindView(
        context: Context,
        position: Int,
        productCatalogue: ProductCatalogue?,
        dashLandingNavigationListener: OnDashLandingNavigationListener
    ) {

        itemView.dashCategoryTitle.text = productCatalogue?.headerText
        itemView.rvDashCategories?.apply {
            val bannerCarouselAdapter = DashCategoryAdapter(context, dashLandingNavigationListener, null)
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = bannerCarouselAdapter
            productCatalogue?.let {
                bannerCarouselAdapter.setData(it)
            }
        }
    }
}

class BannerGridLayoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindView(
        context: Context,
        position: Int,
        productCatalogue: ProductCatalogue?,
        dashLandingNavigationListener: OnDashLandingNavigationListener
    ) {
        itemView.dashCategoryTitle.text = productCatalogue?.headerText
        itemView.rvDashCategories?.apply {
            val bannerGridAdapter = DashCategoryAdapter(context, dashLandingNavigationListener, null)
            layoutManager = GridLayoutManager(context, 2)
            adapter = bannerGridAdapter
            productCatalogue?.let {
                bannerGridAdapter.setData(it)
            }
        }
    }
}

class ProductCarouselLayoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindView(
        context: Context,
        position: Int,
        productCatalogue: ProductCatalogue?,
        iProductListing: IProductListing
    ) {
        itemView.dashCategoryTitle.text = productCatalogue?.headerText
        itemView.rvDashCategories?.apply {
            val productCarouselAdapter = DashCategoryAdapter(context, null, iProductListing)
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = productCarouselAdapter
            productCatalogue?.let {
                productCarouselAdapter.setData(it)
            }
        }
    }
}

class LongBannerCarouselLayoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindView(
        context: Context,
        position: Int,
        productCatalogue: ProductCatalogue?,
        dashLandingNavigationListener: OnDashLandingNavigationListener
    ) {
        itemView.dashCategoryTitle.text = productCatalogue?.headerText
        itemView.rvDashCategories?.apply {
            val longBannerCarouselAdapter = DashCategoryAdapter(context, dashLandingNavigationListener, null)
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = longBannerCarouselAdapter
            productCatalogue?.let {
                longBannerCarouselAdapter.setData(it)
            }
        }
    }
}

class LongBannerListLayoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindView(
        context: Context,
        position: Int,
        productCatalogue: ProductCatalogue?,
        dashLandingNavigationListener: OnDashLandingNavigationListener
    ) {
        itemView.dashCategoryTitle.text = productCatalogue?.headerText
        itemView.rvDashCategories?.apply {
            val longBannerListAdapter = DashCategoryAdapter(context, dashLandingNavigationListener, null)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = longBannerListAdapter
            productCatalogue?.let {
                longBannerListAdapter.setData(it)
            }
        }
    }
}


