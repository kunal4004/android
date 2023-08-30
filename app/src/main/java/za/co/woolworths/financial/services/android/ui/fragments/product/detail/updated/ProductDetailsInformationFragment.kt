package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentProdcutDetailsInformationBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ProductDetailsInformationFragment : BaseFragmentBinding<FragmentProdcutDetailsInformationBinding>(FragmentProdcutDetailsInformationBinding::inflate) {
    var description: String = ""
    var productCode: String = ""

    companion object {
        fun newInstance(description: String?, productCode: String?) = ProductDetailsInformationFragment().withArgs {
            putString("DESCRIPTION", description)
            putString("PRODUCT_CODE", productCode)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            description = getString("DESCRIPTION", "")
            productCode = getString("PRODUCT_CODE", "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProductDetailsInformation()
        binding.productCodeText.text = productCode
        setUniqueIds()
    }

    private fun setProductDetailsInformation() {
        activity?.apply {
            val head = ("<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>" +
                    "@font-face {font-family: 'opensans_regular';src: url('file://"
                    + this.filesDir.absolutePath + "/fonts/OpenSans-Regular.ttf');}" +
                    "body {" +
                    "line-height: 110%;" +
                    "font-size:15px !important;" +
                    "text-align: justify;" +
                    "color:grey;" +
                    "line-height: 1.4;" +
                    "font-family:'opensans_regular';}" +
                    "</style>" +
                    "</head>")

            var descriptionWithoutExtraTag = ""
            if (!TextUtils.isEmpty(description)) {
                descriptionWithoutExtraTag = description.replace("</ul>\n\n<ul>\n".toRegex(), " ")
                        .replace("<p>&nbsp;</p>".toRegex(), "")
                        .replace("<ul><p>&nbsp;</p></ul>".toRegex(), " ")
            }

            val htmlData = ("<!DOCTYPE html><html>"
                    + head
                    + "<body>"
                    + descriptionWithoutExtraTag
                    + "</body></html>")

            binding.webDescription.loadDataWithBaseURL("file:///android_res/drawable/",
                    htmlData, "text/html; charset=UTF-8", "UTF-8", null)
        }
    }

    private fun setUniqueIds() {
        resources?.apply {
            binding.productCodeText?.contentDescription = getString(R.string.pdp_textProductCode)
            binding.title?.contentDescription = getString(R.string.pdp_productDetailsTitle)
            binding.productCodeTitle?.contentDescription = getString(R.string.pdp_productCodeTitle)
        }
    }
}