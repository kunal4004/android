package za.co.woolworths.financial.services.android.chanel.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.chanel.model.DynamicBanner
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.chanel.views.viewholder.ChanelAppBannerViewHolder
import za.co.woolworths.financial.services.android.chanel.views.viewholder.ChanelCategoryNavigationViewHolder
import za.co.woolworths.financial.services.android.chanel.views.viewholder.ChanelHeaderBannerViewHolder
import za.co.woolworths.financial.services.android.chanel.views.viewholder.ChanelLogoViewHolder

class BrandLandingAdapter(val context: Context?, val list: List<DynamicBanner?>, val chanelNavigationClickListener: ChanelNavigationClickListener) :
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
        if (viewType == VIEW_TYPE_LOGO) {
            return ChanelLogoViewHolder(
                LayoutInflater.from(context).inflate(R.layout.chanel_logo_view, parent, false)
            )
        } else if (viewType == VIEW_TYPE_APP_BANNER) {
            return ChanelAppBannerViewHolder(
                LayoutInflater.from(context).inflate(R.layout.chanel_app_banner_view, parent, false)
            )
        } else if (viewType == VIEW_TYPE_NAVIGATION) {
            return ChanelCategoryNavigationViewHolder(
                LayoutInflater.from(context).inflate(R.layout.chanel_category_navigation_view, parent, false),
                chanelNavigationClickListener
            )
        }
        return ChanelHeaderBannerViewHolder(
            LayoutInflater.from(context).inflate(R.layout.chanel_header_banner_view, parent, false), parent
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ChanelLogoViewHolder) {
            holder.bind(position, list)
        } else if (holder is ChanelAppBannerViewHolder) {
            holder.bind(position, list)
        } else if (holder is ChanelAppBannerViewHolder) {
            holder.bind(position, list)
        } else if (holder is ChanelCategoryNavigationViewHolder) {
            holder.bind(position, list, context)
        } else if (holder is ChanelHeaderBannerViewHolder) {
            holder.bind(position, list, context, chanelNavigationClickListener)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {

        if (list.get(position)?.name.equals(LOGO)) {
            return VIEW_TYPE_LOGO
        } else if (list.get(position)?.name.equals(APP_BANNER)) {
            return VIEW_TYPE_APP_BANNER
        } else if (list.get(position)?.name.equals(NAVIGATION)) {
            return VIEW_TYPE_NAVIGATION
        } else
            return VIEW_TYPE_HEADER_BANNER
    }
}