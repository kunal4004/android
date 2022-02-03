package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.chanel_category_navigation_view.view.*
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.DynamicBanner
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.chanel.views.adapter.CategoryNavigationAdapter

class ChanelCategoryNavigationViewHolder(
    itemView: View,
    val chanelNavigationClickListener: ChanelNavigationClickListener
) : RecyclerView.ViewHolder(itemView) {

    fun bind(position: Int, list: List<DynamicBanner?>, context: Context?) {
        if(position >= list.size || position < 0){
            return
        }

        list[position]?.navigation?.let {
            val adapter = CategoryNavigationAdapter(context, it, chanelNavigationClickListener)
            val itemDecor = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)

            itemView.rv_category.layoutManager = LinearLayoutManager(context)
            itemView.rv_category.adapter = adapter
            itemView.rv_category.addItemDecoration(itemDecor)
        }
    }
}