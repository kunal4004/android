package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.barcode_scan_activity.*
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.PDP_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.barcode.BarcodeScanFragment
import za.co.woolworths.financial.services.android.util.RuntimePermissionActivity
import za.co.woolworths.financial.services.android.util.Utils

class BarcodeScanActivity : RuntimePermissionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.barcode_scan_activity)
        Utils.updateStatusBarBackground(this, R.color.black)
        configureActionBar()
        if (savedInstanceState == null) {
            addFragment(
                    fragment = BarcodeScanFragment.newInstance(),
                    tag = BarcodeScanFragment::class.java.simpleName,
                    containerViewId = R.id.flBarcodeScanContainer)
        }
        setUpRuntimePermission(arrayListOf(android.Manifest.permission.CAMERA))
    }

    private fun configureActionBar() {
        toolbar?.apply { setSupportActionBar(this) }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    internal fun setHomeIndicator(isManualScanFragment: Boolean) {
        supportActionBar?.setHomeAsUpIndicator(if (isManualScanFragment) R.drawable.back_white else R.drawable.close_white)
        tvToolbarTitle?.text = if (isManualScanFragment) getString(R.string.enter_barcode) else getString(R.string.scan_product)
        toolbar?.setBackgroundColor(ContextCompat.getColor(this, if (isManualScanFragment) R.color.black else R.color.black))
    }

    override fun onBackPressed() {
        supportFragmentManager?.apply {
            if (backStackEntryCount > 0) {
                popBackStack()
                setHomeIndicator(backStackEntryCount == 0)
            } else {
                finish()
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PDP_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            setResult(ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE, data)
            finish()
            overridePendingTransition(0, 0)
            return
        }
        supportFragmentManager?.findFragmentById(R.id.flBarcodeScanContainer)?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRuntimePermissionRequestGranted() {
        supportFragmentManager?.findFragmentById(R.id.flBarcodeScanContainer)?.apply {
            when (this) {
                is BarcodeScanFragment -> {
                    (this as? BarcodeScanFragment)?.startPreview()
                }
            }
        }
    }

    override fun onRuntimePermissonRequestDenied() = onBackPressed()
}