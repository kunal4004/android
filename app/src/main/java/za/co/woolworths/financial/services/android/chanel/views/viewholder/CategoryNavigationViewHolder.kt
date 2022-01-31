package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.synthetic.main.chanel_catagories_navigation_item.view.*
import za.co.woolworths.financial.services.android.chanel.model.Navigation

class CategoryNavigationViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(position: Int, list: List<Navigation>) {
        itemView.rv_category_name.text = list.get(position).displayName
    }
}