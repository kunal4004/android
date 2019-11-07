package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_prodcut_details_information.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class ProductDetailsInformationFragment : Fragment() {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_prodcut_details_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProductDetailsInformation()
        productCodeText.text = productCode
    }

    private fun setProductDetailsInformation() {
        activity?.apply {
            val head = ("<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>" +
                    "@font-face {font-family: 'myriad-pro-regular';src: url('file://"
                    + this.filesDir.absolutePath + "/fonts/myriadpro_regular.otf');}" +
                    "body {" +
                    "line-height: 110%;" +
                    "font-size:15px !important;" +
                    "text-align: justify;" +
                    "color:grey;" +
                    "line-height: 1.4;" +
                    "font-family:'myriad-pro-regular';}" +
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

            webDescription.loadDataWithBaseURL("file:///android_res/drawable/",
                    htmlData, "text/html; charset=UTF-8", "UTF-8", null)
        }
    }
}