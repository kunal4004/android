package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.synthetic.main.chanel_catagories_navigation_item.view.*
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.Navigation
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener

class CategoryNavigationViewHolder(
    itemView: View,
    val chanelNavigationClickListener: ChanelNavigationClickListener
) : RecyclerView.ViewHolder(itemView) {

    fun bind(position: Int, list: List<Navigation>) {
        if(position >= list.size || position < 0){
            return
        }
        itemView.rv_category_name.text = list[position].displayName
        itemView.setOnClickListener {
            chanelNavigationClickListener.openCategoryListView(list[position])
        }
    }
}