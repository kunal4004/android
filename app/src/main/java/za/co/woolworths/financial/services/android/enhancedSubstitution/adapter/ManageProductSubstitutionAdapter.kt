package za.co.woolworths.financial.services.android.enhancedSubstitution.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.LayoutManageSubstitutionBinding
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.ProductSubstitutionListListener
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

class ManageProductSubstitutionAdapter(
    private var headerItem: SubstitutionRecylerViewItem.SubstitutionOptionHeader,
    private var substitutionProductList: List<SubstitutionRecylerViewItem.SubstitutionProducts>,
    private var productSubstitutionListListener: ProductSubstitutionListListener
) : RecyclerView.Adapter<SubstitutionViewHolder>() {

    companion object {
        const val VIEW_TYPE_SUBSTITUTION_HEADER = 0
        const val VIEW_TYPE_SUBSTITUTION_LIST = 1
    }

    private var lastSelectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubstitutionViewHolder {
        when (viewType) {
            VIEW_TYPE_SUBSTITUTION_HEADER -> return SubstitutionViewHolder.SubstituteOptionHolder(
                LayoutManageSubstitutionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            VIEW_TYPE_SUBSTITUTION_LIST -> return SubstitutionViewHolder.SubstituteProductViewHolder(
                    ShoppingListCommerceItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), parent.context)

            else -> {
                FirebaseManager.logException("Wrong ViewType passed")
                throw IllegalArgumentException("Wrong ViewType Passed")
            }
        }
    }

    override fun onBindViewHolder(holder: SubstitutionViewHolder, position: Int) {
        when (holder) {
            is SubstitutionViewHolder.SubstituteOptionHolder -> {
                holder.bind(headerItem, productSubstitutionListListener)
            }
            is SubstitutionViewHolder.SubstituteProductViewHolder ->  {
                holder.binding.cbShoppingList.isChecked = lastSelectedPosition == position
                holder.bind(substitutionProductList[position])
                holder.binding.cbShoppingList.setOnClickListener {
                    lastSelectedPosition = position
                    notifyItemChanged(position)
                    productSubstitutionListListener?.clickOnSubstituteProduct()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return substitutionProductList.size
    }

    override fun getItemViewType(position: Int): Int {
        if(isPositionHeader(position))
            return VIEW_TYPE_SUBSTITUTION_HEADER
        return VIEW_TYPE_SUBSTITUTION_LIST
    }

    private fun isPositionHeader(position: Int): Boolean {
        return  position == 0
    }


}