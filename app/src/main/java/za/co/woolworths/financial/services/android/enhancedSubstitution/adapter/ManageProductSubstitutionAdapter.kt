package za.co.woolworths.financial.services.android.enhancedSubstitution.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.LayoutManageSubstitutionBinding
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.ProductSubstitutionListListener
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

class ManageProductSubstitutionAdapter(
    var headerItem: SubstitutionRecylerViewItem.SubstitutionOptionHeader,
    var subStitutionProductList: List<SubstitutionRecylerViewItem.SubstitutionProducts>,
    var productSubstitutionListListener: ProductSubstitutionListListener
) : RecyclerView.Adapter<SubstitutionViewHolder>() {

    companion object {
        const val VIEW_TYPE_SUBSTITUTION_HEADER = 0
        const val VIEW_TYPE_SUBSTITUTION_LIST = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubstitutionViewHolder {
        when (viewType) {
            VIEW_TYPE_SUBSTITUTION_HEADER -> return SubstitutionViewHolder.SubstitueOptionwHolder(
                LayoutManageSubstitutionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            VIEW_TYPE_SUBSTITUTION_LIST -> return SubstitutionViewHolder.SubstitueProductViewHolder(
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
            is SubstitutionViewHolder.SubstitueOptionwHolder ->
                holder.bind(headerItem, productSubstitutionListListener)
            is SubstitutionViewHolder.SubstitueProductViewHolder ->
                holder.bind(subStitutionProductList[position])
        }
    }

    override fun getItemCount(): Int {
        return subStitutionProductList.size
    }

    override fun getItemViewType(position: Int): Int {
        if(isPositionHeader(position))
            return VIEW_TYPE_SUBSTITUTION_HEADER;
        return VIEW_TYPE_SUBSTITUTION_LIST;
    }

    private fun isPositionHeader(position: Int): Boolean {
        return  position == 0
    }

}