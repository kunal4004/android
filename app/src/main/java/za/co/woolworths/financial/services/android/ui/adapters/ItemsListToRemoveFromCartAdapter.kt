package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.CartProductItemBinding
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils

class ItemsListToRemoveFromCartAdapter(var commerceItems: ArrayList<CommerceItem>) : RecyclerView.Adapter<ItemsListToRemoveFromCartAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return commerceItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(commerceItems[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CartProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    class ViewHolder(val itemBinding: CartProductItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(commerceItem: CommerceItem) {
            itemBinding.apply {
                swipe.isSwipeEnabled = false
                tvTitle.text = commerceItem.commerceItemInfo.productDisplayName ?: ""
                Utils.truncateMaxLine(tvTitle)
                llQuantity.visibility = View.INVISIBLE
                price.text = CurrencyFormatter.formatAmountToRandAndCentWithSpace(commerceItem.priceInfo.amount)
                rlDeleteButton.visibility = View.GONE
                cartProductImage.setImageURI(commerceItem.commerceItemInfo.externalImageRefV2
                        ?: "")
                if (commerceItem.priceInfo.discountedAmount > 0) {
                    promotionalText.text = " " + CurrencyFormatter.formatAmountToRandAndCentWithSpace(commerceItem.priceInfo.discountedAmount)
                    promotionalTextLayout.visibility = View.VISIBLE
                } else {
                    promotionalTextLayout.visibility = View.GONE
                }
    
                if (commerceItem.commerceItemClassType == "foodCommerceItem") {
                    tvSize.visibility = View.INVISIBLE
                } else {
                    var sizeAndColor = commerceItem.commerceItemInfo?.color ?: ""
                    commerceItem.commerceItemInfo?.apply {
                        if (sizeAndColor.isEmpty() && size.isNotEmpty() && !size.equals("NO SZ", true))
                            sizeAndColor = size
                        else if (sizeAndColor.isNotEmpty() && size.isNotEmpty() && !size.equals("NO SZ", true)) {
                            sizeAndColor = "$sizeAndColor, $size"
                        }
                        tvSize.text = sizeAndColor
                        tvSize.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}