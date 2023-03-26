package za.co.woolworths.financial.services.android.enhancedSubstitution.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.ProductListSelectionListener
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

class SearchProductSubstitutionAdapter(var productListSelectionListener: ProductListSelectionListener) :
        PagingDataAdapter<ProductList, SubstitutionViewHolder>(Comparator) {

   private var lastSelectedPosition:Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubstitutionViewHolder {
        return SubstitutionViewHolder.SubstitueProductViewHolder(
                ShoppingListCommerceItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                ), parent.context, lastSelectedPosition)
    }

    override fun onBindViewHolder(holder: SubstitutionViewHolder, position: Int) {
        when (holder) {
            is SubstitutionViewHolder.SubstitueProductViewHolder -> {
                holder.bind(getItem(position), productListSelectionListener, position)
            }
            else -> {
                FirebaseManager.logException("Wrong ViewType passed")
                throw IllegalArgumentException("Wrong ViewType Passed")
            }
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