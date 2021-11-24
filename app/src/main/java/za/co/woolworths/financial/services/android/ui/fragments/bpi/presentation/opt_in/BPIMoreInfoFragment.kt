package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bpi_more_info_fragment.*
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_MORE_INFO_HTML
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_PRODUCT_GROUP_CODE
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_TERMS_CONDITIONS_HTML
import za.co.woolworths.financial.services.android.util.Utils


class BPIMoreInfoFragment : Fragment()  {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_more_info_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { Utils.updateStatusBarBackground(it, R.color.white) }

        setUpWebView()

        val htmlContent = arguments?.getString(BPI_MORE_INFO_HTML)
        if(htmlContent != null){
            bpiMoreInfoWebView?.loadData(htmlContent, "text/html; charset=utf-8", null)
        }

        bpiCheckBox?.setOnClickListener {
            it.isSelected = !it.isSelected
            optInBpiButton.isEnabled = it.isSelected
        }

        optInBpiButton?.setOnClickListener{
            view.findNavController().navigate(R.id.action_BPIMoreInfoFragment_to_BPIOptInConfirmationFragment)
        }

        bpiCheckBoxDescriptionTextView?.setOnClickListener{
            view.findNavController().navigate(R.id.action_BPIMoreInfoFragment_to_BPITermsAndConditionFragment,
                bundleOf(BPI_TERMS_CONDITIONS_HTML to arguments?.getString(BPI_TERMS_CONDITIONS_HTML),
                    BPI_PRODUCT_GROUP_CODE to arguments?.getString(BPI_PRODUCT_GROUP_CODE)))
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

                    super.onPageFinished(view, url)
                }
            }
        }
    }
}