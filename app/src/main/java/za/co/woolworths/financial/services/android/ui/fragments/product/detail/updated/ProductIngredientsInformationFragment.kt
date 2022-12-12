package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.os.Bundle
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentProdcutIngredientsInformationBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ProductIngredientsInformationFragment : BaseFragmentBinding<FragmentProdcutIngredientsInformationBinding>(FragmentProdcutIngredientsInformationBinding::inflate) {
    var ingredients: String = ""

    companion object {
        fun newInstance(ingredients: String?) = ProductIngredientsInformationFragment().withArgs {
            putString("INGREDIENTS", ingredients)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            ingredients = getString("INGREDIENTS", "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ingredientText.text = ingredients
        setUniqueIds()
    }

    private fun setUniqueIds() {
        resources?.apply {
            binding.title?.contentDescription = getString(R.string.pdp_productIngredientsTitle)
            binding.ingredientText?.contentDescription = getString(R.string.pdp_textViewIngredient)
        }
    }
}