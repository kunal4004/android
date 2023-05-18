package za.co.woolworths.financial.services.android.enhancedSubstitution.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.Item
import za.co.woolworths.financial.services.android.enhancedSubstitution.utils.listener.ProductSubstitutionListListener

class ManageProductSubstitutionAdapter(
    var substitutionProductList: ArrayList<Item>,
    private var productSubstitutionListListener: ProductSubstitutionListListener,
    var isShopperchooseOptionSelected:Boolean = false,
) : RecyclerView.Adapter<SubstitutionViewHolder>() {

    private var lastSelectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubstitutionViewHolder {

        return SubstitutionViewHolder.SubstituteProductViewHolder(
                    ShoppingListCommerceItemBinding.inflate(
                            LayoutInflater.from(parent.context), parent, false
                    ), parent.context)
    }

    override fun onBindViewHolder(holder: SubstitutionViewHolder, position: Int) {
        when (holder) {
            is SubstitutionViewHolder.SubstituteProductViewHolder -> {
                holder.bind(substitutionProductList.getOrNull(position), isShopperchooseOptionSelected)
                if (lastSelectedPosition == position) {
                    holder.binding.cbShoppingList.isChecked = true
                } else {
                    holder.binding.cbShoppingList.isChecked = false
                }
                holder.binding.cbShoppingList.setOnClickListener {
                    lastSelectedPosition = position
                    notifyDataSetChanged()
                    productSubstitutionListListener.clickOnSubstituteProduct(substitutionProductList.getOrNull(position))
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return substitutionProductList.size
    }
}