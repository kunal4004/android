package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.chanel_app_banner_view.view.*
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.DynamicBanner
import za.co.woolworths.financial.services.android.util.ImageManager

class ChanelAppBannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(position: Int, list: List<DynamicBanner?>) {
        ImageManager.setPicture(itemView.img_view_banner, list.get(position)?.externalImageRefV2.toString())
    }
}