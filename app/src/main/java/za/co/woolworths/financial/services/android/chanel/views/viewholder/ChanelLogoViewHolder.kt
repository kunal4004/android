package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.chanel_logo_view.view.*
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.DynamicBanner
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.ImageManager

class ChanelLogoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(position: Int, list: List<DynamicBanner?>) {

        if (list.get(position)?.label.isNullOrEmpty() && list.get(position)?.externalImageRefV2.isNullOrEmpty()) {
            itemView.tv_logo_name.visibility = View.GONE
            itemView.img_view_logo.visibility = View.GONE
            return
        }
        if (list.get(position)?.label == null || list.get(position)?.label == AppConstant.EMPTY_STRING) {
            itemView.tv_logo_name.visibility = View.GONE
            itemView.img_view_logo.visibility = View.VISIBLE
            list.get(position)?.externalImageRefV2?.let {
                ImageManager.loadImage(itemView.img_view_logo,
                    it
                )
            }
        } else {
            itemView.tv_logo_name.visibility = View.VISIBLE
            itemView.img_view_logo.visibility = View.GONE
            itemView.tv_logo_name.text = list.get(position)?.label
        }
    }
}