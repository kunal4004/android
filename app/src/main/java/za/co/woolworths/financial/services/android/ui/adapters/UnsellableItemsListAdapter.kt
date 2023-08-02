package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.LayoutUnsellableItemBinding
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils

class UnsellableItemsListAdapter(var commerceItems: ArrayList<UnSellableCommerceItem>) :
    RecyclerView.Adapter<UnsellableItemsListAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return commerceItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(commerceItems[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutUnsellableItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    class ViewHolder(val itemBinding: LayoutUnsellableItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(commerceItem: UnSellableCommerceItem) {
            itemBinding.apply {
                swipe.isSwipeEnabled = false
                quantityTextView.text = commerceItem.quantity.toString()
                tvTitle.text = " x " + commerceItem.productDisplayName
                Utils.truncateMaxLine(tvTitle)
                tvPrice.text = commerceItem.price.amount.let {
                    CurrencyFormatter.formatAmountToRandAndCentWithSpace(it)
                }
                cartProductImage.setImageURI(commerceItem.externalImageRefV2 ?: "")
            }
        }
    }
}