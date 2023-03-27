package za.co.woolworths.financial.services.android.enhancedSubstitution.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.awfs.coordination.R
import com.awfs.coordination.databinding.LayoutManageSubstitutionBinding
import com.awfs.coordination.databinding.ShoppingListCommerceItemBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.ProductSubstitutionListListener
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.util.CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace

sealed class SubstitutionViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    class SubstitueOptionwHolder(private val binding: LayoutManageSubstitutionBinding) : SubstitutionViewHolder(binding) {

        fun bind(substitutionProducts: SubstitutionRecylerViewItem.SubstitutionOptionHeader?,
                 productSubstitutionListListener: ProductSubstitutionListListener?) {
            binding.tvSearchProduct?.apply {
                text = substitutionProducts?.searchHint
                onClick {
                    /*navigate to new search screen*/
                    productSubstitutionListListener?.openSubstitutionSearchScreen()
                }
            }

            binding.rbShopperChoose?.apply {
                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        binding.rbOwnSubstitute?.isChecked = false
                        binding.tvSearchProduct?.isEnabled = false
                        productSubstitutionListListener?.clickOnLetMyShooperChooseOption()
                    }
                }
            }

            binding.rbOwnSubstitute?.apply {
                if (binding.rbShopperChoose.isChecked) {
                    binding.rbShopperChoose?.isChecked = false
                }

                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        binding.rbShopperChoose?.isChecked = false
                        binding.tvSearchProduct?.isEnabled = false
                        productSubstitutionListListener?.clickOnMySubstitutioneOption()
                    }
                }
            }
        }
    }

    class SubstitueProductViewHolder(
             val binding: ShoppingListCommerceItemBinding,
            var context: Context
    ) : SubstitutionViewHolder(binding) {

        fun bind(substitutionProducts: SubstitutionRecylerViewItem.SubstitutionProducts?) {
            binding.root.isSwipeEnabled = false
            binding.llQuantity?.visibility = View.GONE
            binding.tvProductAvailability?.visibility = View.INVISIBLE
            binding.tvColorSize?.visibility = View.INVISIBLE

            binding.tvTitle.setTextAppearance(R.style.style_substitution_title)
            binding.tvPromotionText.setTextAppearance(R.style.style_substitution_promotion)
            binding.tvPrice.setTextAppearance(R.style.style_substitution_price)

            binding.tvTitle.text = substitutionProducts?.productTitle
            binding.tvPrice.text = context.resources.getString(R.string.rand_text)
                    .plus("\t").plus(substitutionProducts?.productPrice)
            binding.tvPrice.minHeight = context.resources.getDimension(R.dimen.two_dp).toInt()
            binding.tvPromotionText.visibility = View.VISIBLE
            //binding.tvPromotionText.text = substitutionProducts?.promotionText
            binding.cartProductImage?.setImageURI(substitutionProducts?.productThumbnail)
        }

        fun bind(productList: ProductList?) {
            binding.root.isSwipeEnabled = false
            binding.llQuantity?.visibility = View.GONE
            binding.tvProductAvailability?.visibility = View.INVISIBLE
            binding.tvColorSize?.visibility = View.INVISIBLE

            binding.tvTitle.setTextAppearance(R.style.style_substitution_title)
            binding.tvPromotionText.setTextAppearance(R.style.style_substitution_promotion)
            binding.tvPrice.setTextAppearance(R.style.style_substitution_price)

            binding.tvTitle.text = productList?.productName
            binding.tvPrice.text = formatAmountToRandAndCentWithSpace(productList?.price)
            if (productList?.promotions?.isEmpty() == true) {
                binding.tvPromotionText.visibility = View.GONE
            } else {
                binding.tvPromotionText.visibility = View.VISIBLE
                binding.tvPromotionText.text = productList?.promotions?.getOrNull(0)?.promotionalText
            }
            binding.cartProductImage?.setImageURI(productList?.externalImageRefV2)
        }

    }
}