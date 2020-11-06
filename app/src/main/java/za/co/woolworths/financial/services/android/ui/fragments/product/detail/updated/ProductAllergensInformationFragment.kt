package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_prodcut_allergens_information.*
import kotlinx.android.synthetic.main.fragment_prodcut_ingredients_information.title
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class ProductAllergensInformationFragment : Fragment(){
    var allergens: String = ""

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_prodcut_allergens_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allergensText.text = allergens
        setUniqueIds()
    }

    private fun setUniqueIds() {
        resources?.apply {
            title?.contentDescription = getString(R.string.pdp_productAllergensTitle)
            allergensText?.contentDescription = getString(R.string.pdp_textViewAllergens)
        }
    }
}