package za.co.woolworths.financial.services.android.enhancedSubstitution.view

import android.content.Context
import android.view.View
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.Item
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.util.CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace
import za.co.woolworths.financial.services.android.util.Utils

sealed class SubstitutionViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    class SubstituteProductViewHolder(
        val binding: ShoppingListCommerceItemBinding,
        var context: Context,
    ) : SubstitutionViewHolder(binding) {

        fun bind(item: Item?) {
            binding.apply {
                root.isSwipeEnabled = false
                llQuantity.visibility = View.GONE
                tvProductAvailability.visibility = View.INVISIBLE
                tvColorSize.visibility = View.INVISIBLE

                TextViewCompat.setTextAppearance(tvTitle, R.style.style_substitution_title)
                TextViewCompat.setTextAppearance(tvPrice, R.style.style_substitution_price)

                tvTitle.text = item?.title
                tvPrice.minHeight = context.resources.getDimension(R.dimen.two_dp).toInt()
                if (Utils.getDeliveryDetails().isNullOrEmpty()) {
                    tvPrice.text = formatAmountToRandAndCentWithSpace(item?.defaultPrice)
                } else {
                    tvPrice.text = formatAmountToRandAndCentWithSpace(item?.price)
                }
                binding.tvPromotionText.text = item?.PROMOTION
                cartProductImage.setImageURI(item?.imageLink)
            }
        }

        fun bind(productList: ProductList?) {
            binding.apply {
                root.isSwipeEnabled = false
                llQuantity.visibility = View.GONE
                tvProductAvailability.visibility = View.INVISIBLE
                tvColorSize.visibility = View.INVISIBLE

                TextViewCompat.setTextAppearance(tvTitle, R.style.style_substitution_title)
                TextViewCompat.setTextAppearance(tvPrice, R.style.style_substitution_price)

                tvTitle.text = productList?.productName
                tvPrice.text = formatAmountToRandAndCentWithSpace(productList?.price)
                if (productList?.promotions.isNullOrEmpty()) {
                    tvPromotionText.visibility = View.VISIBLE
                    tvPromotionText.setText(productList?.promotions?.getOrNull(0)?.promotionalText)
                } else {
                    tvPromotionText.visibility = View.INVISIBLE
                }
                cartProductImage.setImageURI(productList?.externalImageRefV2)
            }
        }
    }
}