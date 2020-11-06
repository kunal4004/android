package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.awfs.coordination.R

import kotlinx.android.synthetic.main.select_your_quantity_row.view.*

class SelectQuantityViewHolder(parent: ViewGroup) : RecyclerViewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.select_your_quantity_row, parent, false)) {
    fun setItem(quantityInStockList: Int, clickListener: (Int) -> Unit) {
        itemView.tvQuantityValue?.text = quantityInStockList.toString()
        itemView.setOnClickListener { clickListener(quantityInStockList) }
    }
}