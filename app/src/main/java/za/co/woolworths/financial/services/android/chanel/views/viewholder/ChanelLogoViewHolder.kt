package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.chanel_logo_view.view.*
import za.co.woolworths.financial.services.android.chanel.model.DynamicBanner

class ChanelLogoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(position: Int, list: List<DynamicBanner>) {
        itemView.tv_logo_name.text = list.get(position).label
    }
}