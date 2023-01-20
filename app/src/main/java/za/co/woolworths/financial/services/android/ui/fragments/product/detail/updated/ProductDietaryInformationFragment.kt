package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentProdcutDietaryInformationBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ProductDietaryInformationFragment : BaseFragmentBinding<FragmentProdcutDietaryInformationBinding>(FragmentProdcutDietaryInformationBinding::inflate){
    var dietary: String = ""

    companion object {
        fun newInstance(dietary: String?) = ProductDietaryInformationFragment().withArgs {
            putString("DIETARY_INFO", dietary)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            dietary = getString("DIETARY_INFO", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_prodcut_dietary_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dietaryText.text = getStyledText(dietary)
        setUniqueIds()
    }

    private fun setUniqueIds() {
        resources.apply {
            binding.dietaryTitle?.contentDescription = getString(R.string.pdp_productDietaryTitle)
            binding.dietaryText?.contentDescription = getString(R.string.pdp_textViewDietary)
        }
    }

    private fun getStyledText (dietary: String?): SpannableString {
        val spannable = SpannableString(dietary)
        val index = dietary?.indexOf(':')
        if (index != null) {
            spannable.setSpan(StyleSpan(R.style.futura_semi_bold_black), 0, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Color.GRAY), index + 2 , dietary.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spannable
    }
}