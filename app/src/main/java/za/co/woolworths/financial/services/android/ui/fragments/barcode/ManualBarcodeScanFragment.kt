package za.co.woolworths.financial.services.android.ui.fragments.barcode

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.scan_barcode_manual_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.card.BarcodeScannerActivity
import za.co.woolworths.financial.services.android.util.KeyboardUtil

class ManualBarcodeScanFragment : Fragment() {

    companion object {
        fun newInstance() = ManualBarcodeScanFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? BarcodeScannerActivity)?.configureToolbar(R.string.enter_barcode, R.drawable.back_white)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.scan_barcode_manual_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textEvent()
        bottomViewGroup?.visibility = GONE

    }

    private fun textEvent() {
        editBarcodeNumber?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                bottomViewGroup?.visibility = if (s.isNotEmpty()) VISIBLE else GONE
            }
        })
    }

    private fun hideKeyboard() {
        activity?.apply { KeyboardUtil.hideSoftKeyboard(this) }
    }


    override fun onResume() {
        super.onResume()
        activity?.apply { showSoftKeyboard(this, editBarcodeNumber) }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }
}