package za.co.woolworths.financial.services.android.ui.activities.card

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_activity.*
import kotlinx.android.synthetic.main.scan_barcode_activity.*
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.barcode.ManualBarcodeScanFragment
import za.co.woolworths.financial.services.android.ui.fragments.barcode.ScanBarcodeFragment
import za.co.woolworths.financial.services.android.util.Utils

class BarcodeScannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scan_barcode_activity)
        Utils.updateStatusBarBackground(this, R.color.black)
        actionBar()
        if (savedInstanceState == null) {
            addFragment(
                    fragment = ScanBarcodeFragment.newInstance(),
                    tag = ScanBarcodeFragment::class.java.simpleName,
                    containerViewId = R.id.flScanBarcodeContainer
            )
        }

        imNavigateBack.setOnClickListener { navigateBack() }
    }

    private fun actionBar() {
        setSupportActionBar(tbMyCard)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setDisplayHomeAsUpEnabled(false) // remove the left caret
            setDisplayShowHomeEnabled(false) // remove the icon
            configureToolbar(R.string.scan_product, R.drawable.close_white)
        }

    }

    override fun onBackPressed() {
        navigateBack()
    }

    private fun navigateBack() {
        supportFragmentManager?.apply {
            if (backStackEntryCount > 0) {
                // reset toolbar to scan product on back pressed
                when (getCurrentFragment()) {
                    is ManualBarcodeScanFragment -> {
                        configureToolbar(R.string.scan_product, R.drawable.close_white)
                    }
                }
                popBackStack()
            } else finishActivity()
        }
    }

    private fun getCurrentFragment(): Fragment? = supportFragmentManager?.findFragmentById(R.id.flScanBarcodeContainer)

    private fun finishActivity() {
        this.finish()
        this.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    fun configureToolbar(title: Int, backNavigationIcon: Int) {
        resources?.apply {
            tvToolbarText.text = getString(title)
            imNavigateBack?.setImageResource(backNavigationIcon)
        }
    }
}