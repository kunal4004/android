package za.co.woolworths.financial.services.android.ui.activities.online_voucher_redemption

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CartAvailableVouchersToRedeemBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.cart.view.CartFragment.Companion.INTENT_REQUEST_CODE
import za.co.woolworths.financial.services.android.cart.view.CartFragment.Companion.SHOPPING_CART_RESPONSE
import za.co.woolworths.financial.services.android.ui.fragments.voucher_redeemption.AvailableVoucherFragment
import za.co.woolworths.financial.services.android.util.Utils
@AndroidEntryPoint
class AvailableVouchersToRedeemInCart : AppCompatActivity() {

    private lateinit var binding: CartAvailableVouchersToRedeemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CartAvailableVouchersToRedeemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Utils.updateStatusBarBackground(this)
        actionBar()
        loadNavHostFragment()
    }


    private fun actionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        supportFragmentManager?.primaryNavigationFragment?.childFragmentManager?.fragments?.get(0)?.let { fragment ->
            when (fragment) {
                is AvailableVoucherFragment -> {
                    val appliedVouchersCount: Int = fragment.shoppingCartResponse?.data?.get(0)?.voucherDetails?.vouchers?.filter { it.voucherApplied }?.size
                            ?: 0
                    if (fragment.shoppingCartResponse != null && appliedVouchersCount > 0) {
                        setResult(intent.getIntExtra(INTENT_REQUEST_CODE, RESULT_OK), Intent().putExtra(SHOPPING_CART_RESPONSE, Utils.toJson(fragment.shoppingCartResponse)))
                        finish()
                        return
                    }
                }
            }
        }
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    private fun loadNavHostFragment() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val graph = navHostFragment.navController.navInflater.inflate(R.navigation.voucher_redmeeption_nav_graph)
        graph.startDestination = if (intent.hasExtra("VoucherDetails")) R.id.availableVoucherFragment else R.id.applyPromoCodeFragment
        findNavController(R.id.nav_host_fragment).graph = graph
    }


}