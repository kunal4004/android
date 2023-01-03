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
import com.awfs.coordination.databinding.BarcodeManualScanFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.ui.activities.BarcodeScanActivity
import za.co.woolworths.financial.services.android.ui.extension.hideKeyboard
import za.co.woolworths.financial.services.android.ui.extension.showKeyboard
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView
import za.co.woolworths.financial.services.android.util.Utils

class BarcodeManualScanFragment : BarcodeScanExtension(R.layout.barcode_manual_scan_fragment) {

    companion object {
        fun newInstance() = BarcodeManualScanFragment()
    }

    private lateinit var binding: BarcodeManualScanFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.apply {
            (this as? BarcodeScanActivity)?.binding?.setHomeIndicator(true)
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = BarcodeManualScanFragmentBinding.bind(view)

        showKeyboard()
        setEventAndListener()
    }

    override fun onResume() {
        super.onResume()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.SHOP_BARCODE_MANUAL) }
        binding.edtBarcodeNumber?.apply {
            clearFocus()
            requestFocus()
        }
        showKeyboard()
    }

    private fun setEventAndListener() {
        binding.edtBarcodeNumber?.apply {
            setOnKeyPreImeListener(onKeyPreImeListener)
            setOnEditorActionListener { v, actionId, event ->
                if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (binding.edtBarcodeNumber.text.isNotEmpty())
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
                    binding.confirmedBarcodeGroup?.visibility = if (s.isNotEmpty()) VISIBLE else GONE
                }
            })
        }
        binding.btnBarcodeConfirm?.setOnClickListener {
            setAndRetrieveProductDetail()
        }
    }

    private fun setAndRetrieveProductDetail() {
        if (binding.edtBarcodeNumber.text.isEmpty()) return
        if (!getProductDetailAsyncTaskIsRunning)
            binding.edtBarcodeNumber?.text?.toString()?.let { barcodeText ->
                sendResultBack(ProductsRequestParams.SearchType.BARCODE.name, barcodeText)
            }
    }

    private fun showKeyboard() = (activity as? AppCompatActivity)?.let { binding.edtBarcodeNumber?.showKeyboard(it) }

    override fun progressBarVisibility(progressBarIsVisible: Boolean) {
        binding.mProgressBar?.visibility = if (progressBarIsVisible) VISIBLE else GONE
        binding.tvTitle?.visibility = if (progressBarIsVisible) GONE else VISIBLE
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
            binding.edtBarcodeNumber?.hideKeyboard(this)
        }
    }
}