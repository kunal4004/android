package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_prodcut_allergens_information.*
import kotlinx.android.synthetic.main.fragment_prodcut_ingredients_information.title
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class ProductAllergensInformationFragment : Fragment(){
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_prodcut_allergens_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allergensText.text = getStyledText(allergens)
        setUniqueIds()
    }

    private fun setUniqueIds() {
        resources.apply {
            title?.contentDescription = getString(R.string.pdp_productAllergensTitle)
            allergensText?.contentDescription = getString(R.string.pdp_textViewAllergens)
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