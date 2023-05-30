package za.co.woolworths.financial.services.android.enhancedSubstitution.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.listener.ProductListSelectionListener
import za.co.woolworths.financial.services.android.models.dto.ProductList

class SearchProductSubstitutionAdapter(private var productListSelectionListener: ProductListSelectionListener) :
    PagingDataAdapter<ProductList, SubstitutionViewHolder.SubstituteProductViewHolder>(Comparator) {

    private var lastSelectedPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SubstitutionViewHolder.SubstituteProductViewHolder {
        return SubstitutionViewHolder.SubstituteProductViewHolder(
            ShoppingListCommerceItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), parent.context
        )
    }

    override fun onBindViewHolder(
        holder: SubstitutionViewHolder.SubstituteProductViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
        holder.binding.cbShoppingList.isChecked = lastSelectedPosition == position
        holder.binding.cbShoppingList.setOnClickListener {
            lastSelectedPosition = position
            notifyDataSetChanged()
            productListSelectionListener.clickOnProductSelection(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    object Comparator : DiffUtil.ItemCallback<ProductList>() {
        override fun areItemsTheSame(oldItem: ProductList, newItem: ProductList): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: ProductList, newItem: ProductList): Boolean {
            return oldItem == newItem
        }
    }
}
