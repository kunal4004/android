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
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.item_banner_carousel.view.*
import za.co.woolworths.financial.services.android.models.dto.shop.Banner
import za.co.woolworths.financial.services.android.models.dto.shop.ProductCatalogue
import za.co.woolworths.financial.services.android.ui.adapters.shop.dash.DashDeliveryAdapter.Companion.TYPE_EMPTY

class DashCategoryAdapter(
    val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var type: String? = null
    private val diffCallback = object : DiffUtil.ItemCallback<Banner>() {

        override fun areItemsTheSame(oldItem: Banner, newItem: Banner): Boolean {
            return oldItem.displayName == newItem.displayName
        }

        override fun areContentsTheSame(oldItem: Banner, newItem: Banner): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    private val differ = AsyncListDiffer(this, diffCallback)

    private var list: List<Banner>
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
                        .inflate(R.layout.item_banner_carousel, parent, false)
                )
            }
            else -> EmptyViewHolder(View(context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when {
            holder is BannerCarouselItemViewHolder -> {
                holder.bind(context, position, list[position])
            }
            holder is BannerGridItemViewHolder -> {
                holder.bind(context, position, list[position])
            }
            holder is ProductCarouselItemViewHolder -> {
                holder.bind(context, position, list[position])
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

    fun bind(context: Context, position: Int, banner: Banner) {

    }
}
