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
            binding?.apply {
                root.isSwipeEnabled = false
                llQuantity?.visibility = View.GONE
                tvProductAvailability?.visibility = View.INVISIBLE
                tvColorSize?.visibility = View.INVISIBLE

                tvTitle.setTextAppearance(R.style.style_substitution_title)
                tvPrice.setTextAppearance(R.style.style_substitution_price)

                tvTitle.text = substitutionProducts?.productTitle
                tvPrice.text = context.resources.getString(R.string.rand_text)
                        .plus("\t").plus(substitutionProducts?.productPrice)
                tvPrice.minHeight = context.resources.getDimension(R.dimen.two_dp).toInt()
                //binding.tvPromotionText.text = substitutionProducts?.promotionText
                cartProductImage?.setImageURI(substitutionProducts?.productThumbnail)
            }

        }

        fun bind(productList: ProductList?) {
            binding?.apply {
                root.isSwipeEnabled = false
                llQuantity?.visibility = View.GONE
                tvProductAvailability?.visibility = View.INVISIBLE
                tvColorSize?.visibility = View.INVISIBLE

                tvTitle.setTextAppearance(R.style.style_substitution_title)
                tvPrice.setTextAppearance(R.style.style_substitution_price)

                tvTitle.text = productList?.productName
                tvPrice.text = formatAmountToRandAndCentWithSpace(productList?.price)
                cartProductImage?.setImageURI(productList?.externalImageRefV2)
            }

        }

    }
}