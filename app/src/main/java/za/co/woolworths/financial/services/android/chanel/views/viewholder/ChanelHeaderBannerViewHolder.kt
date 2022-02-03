package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


import kotlinx.android.synthetic.main.chanel_header_banner_view.view.*
import za.co.woolworths.financial.services.android.chanel.model.DynamicBanner
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.chanel.views.adapter.ChanelHeaderBannerAdapter


class ChanelHeaderBannerViewHolder(itemView: View, val parent: ViewGroup) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(
        position: Int,
        list: List<DynamicBanner?>,
        context: Context?,
        chanelNavigationClickListener: ChanelNavigationClickListener
    ) {
        list[position]?.products?.let {
            val adapter =
                ChanelHeaderBannerAdapter(
                    context,
                    it, chanelNavigationClickListener
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