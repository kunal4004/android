package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


import kotlinx.android.synthetic.main.chanel_header_banner_view.view.*
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.DynamicBanner
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.chanel.views.adapter.BrandLandingAdapter
import za.co.woolworths.financial.services.android.chanel.views.adapter.ChanelHeaderBannerAdapter
import za.co.woolworths.financial.services.android.util.AppConstant


class ChanelHeaderBannerViewHolder(itemView: View, val parent: ViewGroup) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(
        position: Int,
        list: List<DynamicBanner?>,
        context: Context?,
        chanelNavigationClickListener: ChanelNavigationClickListener
    ) {

        var bannerLabel: String? = AppConstant.EMPTY_STRING;
        var bannerImage: String? = AppConstant.EMPTY_STRING;

        for (banner in list) {
            if (banner?.name.equals(BrandLandingAdapter.LOGO, true)) {
                bannerLabel = banner?.label
                bannerImage = banner?.externalImageRefV2
            }
        }
        list[position]?.products?.let {
            val adapter =
                ChanelHeaderBannerAdapter(
                    context,
                    it, chanelNavigationClickListener,
                    bannerLabel,
                    bannerImage
                )
            val layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL, false
            )
            itemView.tv_header_label.text = list[position]?.headerText
            itemView.rv_products.layoutManager = layoutManager
            itemView.rv_products.adapter = adapter
        }
    }
}