package za.co.woolworths.financial.services.android.enhancedSubstitution.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.awfs.coordination.databinding.LayoutManageSubstitutionBinding
import com.awfs.coordination.databinding.SubstitutionProductsItemCellBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.ProductSubstitutionListListener
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.util.ImageManager

sealed class SubstitutionViewHolder(binding:ViewBinding): RecyclerView.ViewHolder (binding.root) {

    class SubstitueOptionwHolder(private val binding: LayoutManageSubstitutionBinding) : SubstitutionViewHolder(binding) {

        fun bind(substitutionProducts: SubstitutionRecylerViewItem.SubstitutionOptionHeader,
                 productSubstitutionListListener: ProductSubstitutionListListener) {
            binding.tvSearchProduct?.apply {
                text = substitutionProducts.searchHint
                onClick {
                    /*navigate to new search screen*/
                }
            }

            binding.rbShopperChoose?.apply {
                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        binding.rbOwnSubstitute?.isChecked = false
                        binding.tvSearchProduct?.isEnabled = false
                        productSubstitutionListListener.clickOnLetMyShooperChooseOption()
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
                        productSubstitutionListListener.clickOnMySubstitutioneOption()
                    }
                }
            }
        }
    }

    class SubstitueProductViewHolder(
        private val binding: SubstitutionProductsItemCellBinding,
        var context: Context,
    ) : SubstitutionViewHolder(binding) {

        fun bind(substitutionProducts: SubstitutionRecylerViewItem.SubstitutionProducts){
            binding.txtProductTitle.text = substitutionProducts?.productTitle
            binding.txtProductPrice.text = substitutionProducts?.productPrice
            binding.txtPromotionText.text = substitutionProducts?.promotionText
            ImageManager.setPictureWithoutPlaceHolder(binding.productIamge,
                "https://assets.woolworthsstatic.co.za/100-Rye-Bread-400-g-6001009038821.jpg?V=QmO3&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIwLTA5LTA4LzYwMDEwMDkwMzg4MjFfaGVyby5qcGcifQ&")
        }
    }
}