package za.co.woolworths.financial.services.android.enhancedSubstitution.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.Item
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.listener.ProductSubstitutionListListener

class ManageProductSubstitutionAdapter(
    private var substitutionProductList: ArrayList<Item>,
    private var productSubstitutionListListener: ProductSubstitutionListListener
) : RecyclerView.Adapter<SubstitutionViewHolder>() {

    private var lastSelectedPosition = -1
    private var shopperOptionSelected = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubstitutionViewHolder {

        return SubstitutionViewHolder.SubstituteProductViewHolder(
                    ShoppingListCommerceItemBinding.inflate(
                            LayoutInflater.from(parent.context), parent, false
                    ), parent.context)
    }

    override fun onBindViewHolder(holder: SubstitutionViewHolder,  position: Int) {
        when (holder) {
            is SubstitutionViewHolder.SubstituteProductViewHolder -> {
                holder.bind(substitutionProductList.getOrNull(position))
                holder.binding.cbShoppingList.isChecked = lastSelectedPosition == position
                holder.binding.cbShoppingList.setOnClickListener {
                    lastSelectedPosition = position
                    notifyDataSetChanged()
                    productSubstitutionListListener.clickOnSubstituteProduct(substitutionProductList.getOrNull(position))
                }

                if (this.shopperOptionSelected) {
                    holder.binding.cbShoppingList.isClickable = false
                    holder.binding.cbShoppingList.isChecked = false
                } else {
                    holder.binding.cbShoppingList.isClickable = true
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return substitutionProductList.size
    }

    fun setRadioButtonDisabled(disabled: Boolean) {
        this.shopperOptionSelected = disabled
        notifyDataSetChanged()
    }
}