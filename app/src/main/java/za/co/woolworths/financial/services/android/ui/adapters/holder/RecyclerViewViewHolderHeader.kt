package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.item_found_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.ProductList

class RecyclerViewViewHolderHeader(parent: ViewGroup) : RecyclerViewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_found_layout, parent, false)) {

    fun setNumberOfItems(productList: ProductList?) = productList?.numberOfItems?.toString()?.let { numberOfItems -> itemView.tvNumberOfItem.text = numberOfItems }
}