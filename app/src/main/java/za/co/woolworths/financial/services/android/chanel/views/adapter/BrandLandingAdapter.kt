package za.co.woolworths.financial.services.android.chanel.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ChanelAppBannerViewBinding
import com.awfs.coordination.databinding.ChanelCategoryNavigationViewBinding
import com.awfs.coordination.databinding.ChanelHeaderBannerViewBinding
import com.awfs.coordination.databinding.ChanelLogoViewBinding
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.DynamicBanner
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.chanel.views.viewholder.ChanelAppBannerViewHolder
import za.co.woolworths.financial.services.android.chanel.views.viewholder.ChanelCategoryNavigationViewHolder
import za.co.woolworths.financial.services.android.chanel.views.viewholder.ChanelHeaderBannerViewHolder
import za.co.woolworths.financial.services.android.chanel.views.viewholder.ChanelLogoViewHolder

class BrandLandingAdapter(val context: Context?, val list: List<DynamicBanner?>, private val chanelNavigationClickListener: ChanelNavigationClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val LOGO = "Logo"
        const val APP_BANNER = "App Banner"
        const val NAVIGATION = "navigation"

        const val VIEW_TYPE_LOGO = 1
        const val VIEW_TYPE_APP_BANNER = 2
        const val VIEW_TYPE_NAVIGATION = 3
        const val VIEW_TYPE_HEADER_BANNER = 4
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_LOGO -> {
                return ChanelLogoViewHolder(
                    ChanelLogoViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            VIEW_TYPE_APP_BANNER -> {
                return ChanelAppBannerViewHolder(
                    ChanelAppBannerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            VIEW_TYPE_NAVIGATION -> {
                return ChanelCategoryNavigationViewHolder(
                    ChanelCategoryNavigationViewBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    chanelNavigationClickListener
                )
            }
            else -> return ChanelHeaderBannerViewHolder(
               ChanelHeaderBannerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false),parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChanelLogoViewHolder -> {
                holder.bind(position, list)
            }
            is ChanelAppBannerViewHolder -> {
                holder.bind(position, list)
            }
            is ChanelCategoryNavigationViewHolder -> {
                holder.bind(position, list, context)
            }
            is ChanelHeaderBannerViewHolder -> {
                holder.bind(position, list, context, chanelNavigationClickListener)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {

        return when {
            list[position]?.name.equals(LOGO) -> {
                VIEW_TYPE_LOGO
            }
            list[position]?.name.equals(APP_BANNER) -> {
                VIEW_TYPE_APP_BANNER
            }
            list[position]?.name.equals(NAVIGATION) -> {
                VIEW_TYPE_NAVIGATION
            }
            else -> VIEW_TYPE_HEADER_BANNER
        }
    }
}