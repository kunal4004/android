package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_prodcut_ingredients_information.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class ProductIngredientsInformationFragment : Fragment() {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_prodcut_ingredients_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ingredientText.text = ingredients
    }
}