package za.co.woolworths.financial.services.android.enhancedSubstitution.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.Item
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.SubstitutionProducts
import za.co.woolworths.financial.services.android.enhancedSubstitution.utils.listener.ProductSubstitutionListListener
import za.co.woolworths.financial.services.android.util.Utils

class ManageProductSubstitutionAdapter(
    private var substitutionProductList: ArrayList<Item>,
    private var productSubstitutionListListener: ProductSubstitutionListListener
) : RecyclerView.Adapter<SubstitutionViewHolder>() {

    private var lastSelectedPosition = -1
    var isShopperchooseptionSelected = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubstitutionViewHolder {

        return SubstitutionViewHolder.SubstituteProductViewHolder(
                    ShoppingListCommerceItemBinding.inflate(
                            LayoutInflater.from(parent.context), parent, false
                    ), parent.context)
    }

    override fun onBindViewHolder(holder: SubstitutionViewHolder, position: Int) {
        when (holder) {
            is SubstitutionViewHolder.SubstituteProductViewHolder -> {
                if (isShopperchooseptionSelected) {
                    if (holder.binding.cbShoppingList.isChecked) {
                        holder.binding.cbShoppingList.isChecked = false
                    }
                    holder.binding.cbShoppingList.isEnabled = false
                    holder.binding.root?.isEnabled = false
                    Utils.fadeInFadeOutAnimation(
                            holder.binding.root,
                            false
                    )
                } else {
                    holder.binding.cbShoppingList.isEnabled = true
                    holder.binding.root?.isEnabled = true
                    holder.binding.cbShoppingList.isChecked = lastSelectedPosition == position
                    holder.bind(substitutionProductList[position])
                    if (holder.binding.cbShoppingList.isChecked) {
                        productSubstitutionListListener.clickOnSubstituteProduct()
                    }
                    holder.binding.cbShoppingList.setOnClickListener {
                        lastSelectedPosition = position
                        notifyDataSetChanged()
                        productSubstitutionListListener?.clickOnSubstituteProduct()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return substitutionProductList.size
    }

}