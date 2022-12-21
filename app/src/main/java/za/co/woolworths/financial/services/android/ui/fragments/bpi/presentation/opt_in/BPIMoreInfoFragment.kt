package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BpiMoreInfoFragmentBinding
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_PRODUCT_GROUP_CODE
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.carousel.BPIOptInCarouselFragment
import za.co.woolworths.financial.services.android.util.Utils


class BPIMoreInfoFragment : Fragment(R.layout.bpi_more_info_fragment)  {

    private lateinit var binding: BpiMoreInfoFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = BpiMoreInfoFragmentBinding.bind(view)

        activity?.let { Utils.updateStatusBarBackground(it, R.color.white) }

        binding.apply {
            setUpWebView()

            BPIOptInCarouselFragment.htmlContent?.moreInformationHtml?.let {
                bpiMoreInfoWebView?.loadData(it, "text/html; charset=utf-8", null)
            }

            bpiCheckBox?.onClick {
                it.isSelected = !it.isSelected
                optInBpiButton.isEnabled = it.isSelected
            }

            optInBpiButton?.onClick {
                view.findNavController().navigate(
                    R.id.action_BPIMoreInfoFragment_to_BPIOptInConfirmationFragment,
                    bundleOf(BPI_PRODUCT_GROUP_CODE to arguments?.getString(BPI_PRODUCT_GROUP_CODE))
                )
            }

            bpiCheckBoxDescriptionTextView?.onClick {
                view.findNavController().navigate(
                    R.id.action_BPIMoreInfoFragment_to_BPITermsAndConditionFragment,
                    bundleOf(BPI_PRODUCT_GROUP_CODE to arguments?.getString(BPI_PRODUCT_GROUP_CODE))
                )
            }
        }
    }

    override fun onResume() {
        binding.apply {
            bpiCheckBox?.isChecked = optInBpiButton.isEnabled
            bpiCheckBox?.isSelected = optInBpiButton.isEnabled
        }
        (activity as? BalanceProtectionInsuranceActivity)?.changeActionBarUIForBPIOptIn()
        super.onResume()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun BpiMoreInfoFragmentBinding.setUpWebView(){
        bpiMoreInfoWebView?.apply {
            visibility = View.GONE
            with(settings) {
                javaScriptEnabled = true
                allowContentAccess = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
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

                    visibility = View.VISIBLE
                    refreshDrawableState()
                    super.onPageFinished(view, url)
                }
            }
        }
    }
}