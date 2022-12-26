package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.LayoutCartListProductItemBinding
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
            LayoutCartListProductItemBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false)
        )
    }

    class ViewHolder(val itemBinding: LayoutCartListProductItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(commerceItem: UnSellableCommerceItem) {
            itemBinding.apply {
                swipe.isSwipeEnabled = false
                tvTitle.text = commerceItem.productDisplayName ?: ""
                Utils.truncateMaxLine(tvTitle)
                llQuantity.visibility = View.INVISIBLE
                tvPrice.text = commerceItem.price.amount.let {
                    CurrencyFormatter.formatAmountToRandAndCentWithSpace(it)
                }
                rlDeleteButton.visibility = View.GONE
                minusDeleteCountImage.visibility = View.GONE
                cartProductImage.setImageURI(commerceItem.externalImageRefV2 ?: "")
                if (commerceItem.price.getDiscountedAmount() > 0) {
                    promotionalText.text =
                        " ${CurrencyFormatter.formatAmountToRandAndCentWithSpace(commerceItem.price.getDiscountedAmount())}"
                    promotionalTextLayout.visibility = View.VISIBLE
                } else {
                    promotionalTextLayout.visibility = View.GONE
                }

                if (commerceItem.commerceItemClassType == "foodCommerceItem") {
                    tvColorSize.visibility = View.INVISIBLE
                } else {
                    var sizeAndColor = commerceItem.colour ?: ""
                    commerceItem.apply {
                        if (sizeAndColor.isEmpty() && !size.isNullOrEmpty() && !size.equals("NO SZ",
                                true)
                        )
                            sizeAndColor = size
                        else if (sizeAndColor.isNotEmpty() && !size.isNullOrEmpty() && !size.equals(
                                "NO SZ",
                                true)
                        ) {
                            sizeAndColor = "$sizeAndColor, $size"
                        }
                        tvColorSize.text = sizeAndColor
                        tvColorSize.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}