package za.co.woolworths.financial.services.android.ui.fragments.barcode

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.barcode_scan_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.ui.activities.BarcodeScanActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.barcode.AutoFocusMode
import za.co.woolworths.financial.services.android.util.barcode.CodeScanner
import za.co.woolworths.financial.services.android.util.barcode.CodeScannerView
import android.net.Uri
import za.co.woolworths.financial.services.android.ui.views.alert.OnHideAlertListener
import za.co.woolworths.financial.services.android.util.ErrorHandlerView


open class BarcodeScanFragment : BarcodeScanExtension(), OnHideAlertListener {
    private var mCodeScanner: CodeScanner? = null

    companion object {
        fun newInstance() = BarcodeScanFragment()
        private const val SHOW_CODE_SCAN_AFTER_DELAY: Long = 10
        const val DOMAIN_WOOLWORTHS = "woolworths.co.za"
        const val WHITE_LISTED_DOMAIN = "WHITE_LISTED_DOMAIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? BarcodeScanActivity)?.setHomeIndicator(false)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.barcode_scan_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler().postDelayed({
            activity?.apply {
                val codeScannerView = view?.findViewById<CodeScannerView>(R.id.codeScannerView)
                mCodeScanner =
                        codeScannerView?.let {
                            CodeScanner.builder()
                                    .formats(CodeScanner.ALL_FORMATS)
                                    .autoFocusMode(AutoFocusMode.SAFE)
                                    .autoFocusInterval(2000L)
                                    .flash(false)
                                    .onDecoded { result ->
                                        runOnUiThread {
                                            result.text?.apply {
                                                if (!getProductDetailAsyncTaskIsRunning) {
                                                    when (result.barcodeFormat) {
                                                        BarcodeFormat.QR_CODE -> {
                                                            getProductSearchTypeAndSearchTerm(this).let { it->
                                                                with(it.searchTerm) {
                                                                    when {
                                                                        isEmpty() -> {
                                                                            ErrorHandlerView(activity).showToast(getString(R.string.invalid_qr_code),this@BarcodeScanFragment)
                                                                        }
                                                                        contains(WHITE_LISTED_DOMAIN) -> {
                                                                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(this@apply)))
                                                                            finish()
                                                                            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
                                                                        }
                                                                        else -> {
                                                                            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_SCAN_CODE)
                                                                            sendResultBack(it.searchType.name, this)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        else -> {
                                                            sendResultBack(ProductsRequestParams.SearchType.BARCODE.name, this)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .onError { }.build(this, it)
                        }
                startPreview()
            }
        }, SHOW_CODE_SCAN_AFTER_DELAY)

        btnBarcodeManualScan?.setOnClickListener {
            stopPreview()
            replaceFragment(
                    fragment = BarcodeManualScanFragment.newInstance(),
                    tag = BarcodeManualScanFragment::class.java.simpleName,
                    containerViewId = R.id.flBarcodeScanContainer,
                    allowStateLoss = true,
                    enterAnimation = R.anim.slide_in_from_right,
                    exitAnimation = R.anim.slide_to_left,
                    popEnterAnimation = R.anim.slide_from_left,
                    popExitAnimation = R.anim.slide_to_right
            )
        }
    }

    internal fun startPreview() = activity?.apply { runOnUiThread { mCodeScanner?.startPreview() } }

    private fun stopPreview() = mCodeScanner?.releaseResources()

    override fun progressBarVisibility(progressBarIsVisible: Boolean) {
        ppBar?.visibility = if (progressBarIsVisible) VISIBLE else GONE
        tvTitle?.visibility = if (progressBarIsVisible) GONE else VISIBLE
    }

    override fun onSuccess() {
        startPreview()
    }

    override fun onResume() {
        super.onResume()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.SHOP_BARCODE) }
    }

    override fun onDetach() {
        super.onDetach()
        stopPreview()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        startPreview()
    }

    override fun networkConnectionState(isConnected: Boolean) {
        if (isConnected && !networkNotAvailable) {
            mRetrieveProductDetail = retrieveProductDetail()
        }
    }

    override fun onHide() {
        startPreview()
    }

}