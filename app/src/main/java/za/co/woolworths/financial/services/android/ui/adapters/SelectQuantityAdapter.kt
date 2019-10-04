package za.co.woolworths.financial.services.android.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.woolworths.financial.services.android.ui.adapters.holder.SelectQuantityViewHolder

class SelectQuantityAdapter(private val clickListener: (Int) -> Unit) : RecyclerView.Adapter<SelectQuantityViewHolder>() {

    private val quantityInStockList: MutableList<Int>? = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectQuantityViewHolder = SelectQuantityViewHolder(parent)
    override fun getItemCount(): Int = quantityInStockList?.size ?: 0

    override fun onBindViewHolder(holder: SelectQuantityViewHolder, position: Int) {
        quantityInStockList?.get(position)?.let { quantity -> holder.setItem(quantity, clickListener) }
    }

    fun setItem(quantityInStock: Int) {
        for (quantity in 1..quantityInStock step 1)
            quantityInStockList?.add(quantity)
        notifyDataSetChanged()
    }
}
