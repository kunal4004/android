package za.co.woolworths.financial.services.android.enhancedSubstitution.view

import android.content.Context
import android.view.View
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.SubstitutionProducts
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.util.CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace

sealed class SubstitutionViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    class SubstituteProductViewHolder(
        val binding: ShoppingListCommerceItemBinding,
        var context: Context,
    ) : SubstitutionViewHolder(binding) {

        fun bind(substitutionProducts: SubstitutionProducts?) {
            binding?.apply {
                root.isSwipeEnabled = false
                llQuantity?.visibility = View.GONE
                tvProductAvailability?.visibility = View.INVISIBLE
                tvColorSize?.visibility = View.INVISIBLE
                tvTitle.setTextAppearance(context, R.style.style_substitution_title)
                tvPrice.setTextAppearance(context, R.style.style_substitution_price)
                tvTitle.text = substitutionProducts?.productTitle
                tvPrice.text = context.resources.getString(R.string.rand_text)
                    .plus("\t").plus(substitutionProducts?.productPrice)
                tvPrice.minHeight = context.resources.getDimension(R.dimen.two_dp).toInt()
                binding.tvPromotionText.text = substitutionProducts?.promotionText
                cartProductImage?.setImageURI(substitutionProducts?.productThumbnail)
            }
        }

        fun bind(productList: ProductList?) {
            binding?.apply {
                root.isSwipeEnabled = false
                llQuantity?.visibility = View.GONE
                tvProductAvailability?.visibility = View.INVISIBLE
                tvColorSize?.visibility = View.INVISIBLE

                TextViewCompat.setTextAppearance(tvTitle, R.style.style_substitution_title);
                TextViewCompat.setTextAppearance(tvPrice, R.style.style_substitution_price);

                tvTitle.text = productList?.productName
                tvPrice.text = formatAmountToRandAndCentWithSpace(productList?.price)
                if (productList?.promotions?.isNullOrEmpty() == true) {
                    tvPromotionText?.visibility = View.VISIBLE
                    tvPromotionText?.setText(productList?.promotions?.getOrNull(0)?.promotionalText)
                } else {
                    tvPromotionText?.visibility = View.INVISIBLE
                }
                cartProductImage?.setImageURI(productList?.externalImageRefV2)
            }
        }
    }
}