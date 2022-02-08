package za.co.woolworths.financial.services.android.chanel.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.chanel.views.viewholder.ChanelHeaderBannerItemCellViewHolder
import za.co.woolworths.financial.services.android.models.dto.ProductList

class ChanelHeaderBannerAdapter (val context: Context?,
                                 val list: List<ProductList>,
                                 private val chanelNavigationClickListener: ChanelNavigationClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ChanelHeaderBannerItemCellViewHolder(LayoutInflater.from(context).inflate(
            R.layout.chanel_products_horizontal_item_cell, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position >= list.size || position < 0){
            return
        }
        if (holder is ChanelHeaderBannerItemCellViewHolder) {
            holder.setProductItem(list[position], chanelNavigationClickListener)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}