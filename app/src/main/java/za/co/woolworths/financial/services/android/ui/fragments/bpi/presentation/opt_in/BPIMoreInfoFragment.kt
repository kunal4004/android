package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bpi_more_info_fragment.*
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_PRODUCT_GROUP_CODE
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.carousel.BPIOptInCarouselFragment
import za.co.woolworths.financial.services.android.util.Utils


class BPIMoreInfoFragment : Fragment()  {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_more_info_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { Utils.updateStatusBarBackground(it, R.color.white) }

        setUpWebView()

        if(BPIOptInCarouselFragment.htmlContent == null){
            activity?.apply {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder
                    .setTitle("test")
                    .setMessage("BPIOptInCarouselFragment.htmlContent is null")
                    .setCancelable(true)
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }


        if(BPIOptInCarouselFragment.htmlContent?.moreInformationHtml == null){
            activity?.apply {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder
                    .setTitle("test")
                    .setMessage("BPIOptInCarouselFragment.htmlContent.moreInformationHtml is null")
                    .setCancelable(true)
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }

        BPIOptInCarouselFragment.htmlContent?.moreInformationHtml?.let {
            bpiMoreInfoWebView?.loadData(it, "text/html; charset=utf-8", null)
        }

        bpiCheckBox?.onClick {
            it.isSelected = !it.isSelected
            optInBpiButton.isEnabled = it.isSelected
        }

        optInBpiButton?.onClick{
            view.findNavController().navigate(R.id.action_BPIMoreInfoFragment_to_BPIOptInConfirmationFragment,
                bundleOf(BPI_PRODUCT_GROUP_CODE to arguments?.getString(BPI_PRODUCT_GROUP_CODE)))
        }

        bpiCheckBoxDescriptionTextView?.onClick{
            view.findNavController().navigate(R.id.action_BPIMoreInfoFragment_to_BPITermsAndConditionFragment,
                bundleOf(BPI_PRODUCT_GROUP_CODE to arguments?.getString(BPI_PRODUCT_GROUP_CODE)))
        }
    }

    override fun onResume() {
        bpiCheckBox?.isChecked = optInBpiButton.isEnabled
        bpiCheckBox?.isSelected = optInBpiButton.isEnabled
        (activity as? BalanceProtectionInsuranceActivity)?.changeActionBarUIForBPIOptIn()
        super.onResume()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView(){
        bpiMoreInfoWebView?.apply {
            with(settings) {
                javaScriptEnabled = true
            }

            webViewClient = object : android.webkit.WebViewClient() {

                override fun onPageFinished(view: android.webkit.WebView?, url: kotlin.String?) {
                    val inputStream: java.io.InputStream = resources.assets.open("fonts/WebViewFontFaceStyle.css")
                    var inputAsString = inputStream.bufferedReader().use { it.readText() }

                    inputAsString = inputAsString.replace("\\s".toRegex(),"")

                    val js = "var style = document.createElement('style'); " +
                            "style.innerHTML = \"$inputAsString\";" +
                            " document.head.appendChild(style);" +
                            "document.querySelectorAll('body').forEach((element) => { element.classList.add('bpi-notes'); });"
                    bpiMoreInfoWebView.evaluateJavascript(js,null)
                    activity?.apply {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                        builder
                            .setTitle("test webview")
                            .setMessage(url)
                            .setCancelable(true)
                        val dialog: AlertDialog = builder.create()
                        dialog.show()
                    }
                    super.onPageFinished(view, url)
                }
            }
        }
    }
}