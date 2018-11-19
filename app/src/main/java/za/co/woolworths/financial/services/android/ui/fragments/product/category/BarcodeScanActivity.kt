package za.co.woolworths.financial.services.android.ui.fragments.product.category

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.activities.product.refine.ProductsRefineActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.barcode.BarcodeFragment

class BarcodeScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scan)
        val barcodeFragment = BarcodeFragment()
        val bundle = Bundle()
        bundle.putString("SCAN_MODE", "ONE_D_MODE")
        barcodeFragment.arguments = bundle
        replaceFragmentSafely(barcodeFragment, "", false, false, R.id.fragmentContainer)
    }
}
