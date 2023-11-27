package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.SelectYourQuantityRowBinding
import za.co.woolworths.financial.services.android.ui.adapters.holder.SelectQuantityViewHolder

class SelectQuantityAdapter(private val clickListener: (Int) -> Unit) : RecyclerView.Adapter<SelectQuantityViewHolder>() {

    private val quantityInStockList: MutableList<Int>? = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectQuantityViewHolder =
        SelectQuantityViewHolder(
            SelectYourQuantityRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    override fun getItemCount(): Int = quantityInStockList?.size ?: 0

    override fun onBindViewHolder(holder: SelectQuantityViewHolder, position: Int) {
        quantityInStockList?.get(position)?.let { quantity ->
        if (position == quantityInStockList.size -1 ){
            holder.itemBinding.quantitySelectorDevider.visibility = View.GONE
        }
            holder.setItem(quantity, clickListener)
        }
    }

    fun setItem(quantityInStock: Int) {
        for (quantity in 1..quantityInStock step 1)
            quantityInStockList?.add(quantity)
        notifyDataSetChanged()
    }
}
