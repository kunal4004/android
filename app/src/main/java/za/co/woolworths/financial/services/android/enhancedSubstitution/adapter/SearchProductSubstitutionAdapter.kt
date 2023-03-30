package za.co.woolworths.financial.services.android.enhancedSubstitution.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.ProductListSelectionListener
import za.co.woolworths.financial.services.android.models.dto.ProductList

class SearchProductSubstitutionAdapter(var productListSelectionListener: ProductListSelectionListener) :
        PagingDataAdapter<ProductList, SubstitutionViewHolder.SubstitueProductViewHolder>(Comparator) {

    private var lastSelectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubstitutionViewHolder.SubstitueProductViewHolder {
        return SubstitutionViewHolder.SubstitueProductViewHolder(
                ShoppingListCommerceItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                ), parent.context)
    }

    override fun onBindViewHolder(holder: SubstitutionViewHolder.SubstitueProductViewHolder, position: Int) {
        holder.bind(getItem(position))
        if (lastSelectedPosition == position) {
            holder.binding.cbShoppingList.isChecked = true
        } else {
            holder.binding.cbShoppingList.isChecked = false
        }
        holder.binding.cbShoppingList.setOnClickListener {
            lastSelectedPosition = position
            notifyDataSetChanged()
            productListSelectionListener.clickOnProductSelection(getItem(position))
        }
    }
    override fun getItemViewType(position: Int): Int {
        return position;
    }
}
object Comparator : DiffUtil.ItemCallback<ProductList>() {
    override fun areItemsTheSame(oldItem: ProductList, newItem: ProductList): Boolean {
        return oldItem.productId == newItem.productId
    }
    override fun areContentsTheSame(oldItem: ProductList, newItem: ProductList): Boolean {
        return oldItem == newItem
    }
}