package za.co.woolworths.financial.services.android.ui.fragments.barcode

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.barcode_manual_scan_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.ui.activities.BarcodeScanActivity
import za.co.woolworths.financial.services.android.ui.extension.hideKeyboard
import za.co.woolworths.financial.services.android.ui.extension.showKeyboard
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView
import za.co.woolworths.financial.services.android.util.Utils

class BarcodeManualScanFragment : BarcodeScanExtension() {

    companion object {
        fun newInstance() = BarcodeManualScanFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.apply {
            (this as? BarcodeScanActivity)?.setHomeIndicator(true)
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.barcode_manual_scan_fragment, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showKeyboard()
        setEventAndListener()
    }

    override fun onResume() {
        super.onResume()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.SHOP_BARCODE_MANUAL) }
        edtBarcodeNumber?.apply {
            clearFocus()
            requestFocus()
        }
        showKeyboard()
    }

    private fun setEventAndListener() {
        edtBarcodeNumber?.apply {
            setOnKeyPreImeListener(onKeyPreImeListener)
            setOnEditorActionListener { v, actionId, event ->
                if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (edtBarcodeNumber.text.isNotEmpty())
                        setAndRetrieveProductDetail()
                }
                true
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable) {
                    confirmedBarcodeGroup?.visibility = if (s.isNotEmpty()) VISIBLE else GONE
                }
            })
        }
        btnBarcodeConfirm?.setOnClickListener {
            setAndRetrieveProductDetail()
        }
    }

    private fun setAndRetrieveProductDetail() {
        if (edtBarcodeNumber.text.isEmpty()) return
        if (!getProductDetailAsyncTaskIsRunning)
            edtBarcodeNumber?.text?.toString()?.let { barcodeText ->
                sendResultBack(ProductsRequestParams.SearchType.BARCODE.name, barcodeText)
            }
    }

    private fun showKeyboard() = (activity as? AppCompatActivity)?.let { edtBarcodeNumber?.showKeyboard(it) }

    override fun progressBarVisibility(progressBarIsVisible: Boolean) {
        mProgressBar?.visibility = if (progressBarIsVisible) VISIBLE else GONE
        tvTitle?.visibility = if (progressBarIsVisible) GONE else VISIBLE
    }

    private val onKeyPreImeListener = WLoanEditTextView.OnKeyPreImeListener { onBackPressed() }

    private fun onBackPressed() = activity?.apply { this.onBackPressed() }


    override fun networkConnectionState(isConnected: Boolean) {
        if (isConnected && !networkNotAvailable) {
            mRetrieveProductDetail = retrieveProductDetail()
            networkNotAvailable = true
        }
    }

    override fun onDetach() {
        super.onDetach()
        (activity as? AppCompatActivity)?.apply {
            window?.clearFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            edtBarcodeNumber?.hideKeyboard(this)
        }
    }
}