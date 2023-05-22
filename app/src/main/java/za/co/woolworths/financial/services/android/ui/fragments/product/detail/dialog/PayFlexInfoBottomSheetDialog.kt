package za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.awfs.coordination.databinding.PayFlexBottomSheetDialogBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class PayFlexInfoBottomSheetDialog : WBottomSheetDialogFragment() {
    private lateinit var binding: PayFlexBottomSheetDialogBinding



    companion object {

        fun newInstance() = PayFlexInfoBottomSheetDialog()
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PayFlexBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // with(binding) {
           // payflexInfo.loadUrl("https://widgets.payflex.co.za/how_to.html?")
           // payflexInfo.settings.javaScriptEnabled = true

           binding?.apply {
               payflexInfo.webViewClient = object : WebViewClient() {
                   override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                       view?.loadUrl("https://widgets.payflex.co.za/how_to.html?")
                       return true
                   }
               }
               payflexInfo.settings.javaScriptEnabled = true
               payflexInfo.settings.useWideViewPort = true
               payflexInfo.settings.loadWithOverviewMode = true
               payflexInfo.loadUrl("https://widgets.payflex.co.za/how_to.html?")

           }

          //  }



    }



}