package za.co.woolworths.financial.services.android.ui.adapters.holder

import com.awfs.coordination.databinding.SelectYourQuantityRowBinding

class SelectQuantityViewHolder(val itemBinding: SelectYourQuantityRowBinding) : RecyclerViewViewHolder(itemBinding.root) {
    fun setItem(quantityInStockList: Int, clickListener: (Int) -> Unit) {
        itemBinding.tvQuantityValue?.text = quantityInStockList.toString()
        itemBinding.root.setOnClickListener { clickListener(quantityInStockList) }
    }
}