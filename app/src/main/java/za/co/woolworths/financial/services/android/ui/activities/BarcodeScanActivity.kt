package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BarcodeScanActivityBinding
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.barcode.BarcodeScanFragment
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.Companion.ADDED_TO_SHOPPING_LIST_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment.Companion.SHOPPING_LIST_SEARCH_RESULT_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.RuntimePermissionActivity
import za.co.woolworths.financial.services.android.util.Utils

class BarcodeScanActivity : RuntimePermissionActivity() {

    var binding: BarcodeScanActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BarcodeScanActivityBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        Utils.updateStatusBarBackground(this, R.color.black, true)
        binding?.configureActionBar()
        if (savedInstanceState == null) {
            addFragment(
                    fragment = BarcodeScanFragment.newInstance(),
                    tag = BarcodeScanFragment::class.java.simpleName,
                    containerViewId = R.id.flBarcodeScanContainer)
        }
        setUpRuntimePermission(arrayListOf(android.Manifest.permission.CAMERA))
    }

    private fun BarcodeScanActivityBinding.configureActionBar() {
        toolbar?.apply { setSupportActionBar(this) }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    internal fun BarcodeScanActivityBinding.setHomeIndicator(isManualScanFragment: Boolean) {
        supportActionBar?.setHomeAsUpIndicator(if (isManualScanFragment) R.drawable.back_white else R.drawable.close_white)
        tvToolbarTitle?.text = if (isManualScanFragment) getString(R.string.enter_barcode) else getString(R.string.scan_product)
        toolbar?.setBackgroundColor(ContextCompat.getColor(this@BarcodeScanActivity, if (isManualScanFragment) R.color.black else R.color.sem_per_black))
    }

    override fun onBackPressed() {
        supportFragmentManager?.apply {
            if (backStackEntryCount > 0) {
                popBackStack()
                binding?.setHomeIndicator(backStackEntryCount == 0)
            } else {
                finish()
                overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
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

        if (requestCode == SHOPPING_LIST_SEARCH_RESULT_REQUEST_CODE && resultCode == ADDED_TO_SHOPPING_LIST_RESULT_CODE) {
            data?.let { setActivityResult(it, ADDED_TO_SHOPPING_LIST_RESULT_CODE) }
            return
        }

        supportFragmentManager?.findFragmentById(R.id.flBarcodeScanContainer)?.onActivityResult(requestCode, resultCode, data)
    }

    private fun setActivityResult(data: Intent, addToShoppingListResultCode: Int) {
        setResult(addToShoppingListResultCode, data)
        finish()
        overridePendingTransition(0, 0)
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