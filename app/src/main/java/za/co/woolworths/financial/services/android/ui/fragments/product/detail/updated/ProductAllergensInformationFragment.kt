package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentProdcutAllergensInformationBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ProductAllergensInformationFragment : BaseFragmentBinding<FragmentProdcutAllergensInformationBinding>(FragmentProdcutAllergensInformationBinding::inflate){
    private var allergens: String = ""

    companion object {
        fun newInstance(allergens: String?) = ProductAllergensInformationFragment().withArgs {
            putString("ALLERGEN_INFO", allergens)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            allergens = getString("ALLERGEN_INFO", "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.allergensText.text = getStyledText(allergens)
        setUniqueIds()
    }

    private fun setUniqueIds() {
        resources.apply {
            binding.title?.contentDescription = getString(R.string.pdp_productAllergensTitle)
            binding.allergensText?.contentDescription = getString(R.string.pdp_textViewAllergens)
        }
    }

    private fun getStyledText (ingredients: String?): SpannableString {
        val spannable = SpannableString(ingredients)
        val index = ingredients?.indexOf(':')
        if (index != null) {
            spannable.setSpan(StyleSpan(R.style.futura_semi_bold_black), 0, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Color.GRAY), index + 2 , ingredients.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spannable
    }
}