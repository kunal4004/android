package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_checkout.*
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.ProvinceSelectorFragment
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.SuburbSelectorFragment
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment


/**
 * Created by Kunal Uttarwar on 26/05/21.
 */
class CheckoutActivity : AppCompatActivity() {

    var navHostFrag = NavHostFragment()
    var savedAddressResponse: SavedAddressResponse? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        setActionBar()
        intent?.extras?.apply {
            savedAddressResponse = getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
        }
        loadNavHostFragment()
    }

    fun setActionBar() {
        toolbar?.visibility = View.VISIBLE
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun showBackArrowWithoutTitle() {
        toolbar?.visibility = View.VISIBLE
        setSupportActionBar(toolbar)
        toolbarText.text = ""
        supportActionBar?.apply {
            title = ""
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun hideBackArrow() {
        toolbar?.visibility = View.VISIBLE
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(false)
        }
    }

    fun hideToolbar() {
        toolbar?.visibility = View.GONE
    }

    private fun loadNavHostFragment() {
        navHostFrag = navHostFragment as NavHostFragment
        val graph =
            navHostFrag.navController.navInflater.inflate(R.navigation.nav_graph_checkout)

        graph.startDestination = when {
            savedAddressResponse?.addresses.isNullOrEmpty() -> {
                 R.id.CheckoutAddAddressNewUserFragment
            }
            TextUtils.isEmpty(savedAddressResponse?.defaultAddressNickname) -> {
                R.id.checkoutAddressConfirmationFragment
            }
            else -> R.id.CheckoutAddAddressReturningUserFragment
        }
        findNavController(R.id.navHostFragment).setGraph(graph, intent?.extras)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> onBackPressed()
            android.R.id.home -> onBackPressed()
        }
        return false
    }

    override fun onBackPressed() {
        val fragmentList: MutableList<androidx.fragment.app.Fragment> =
            navHostFrag.childFragmentManager.fragments

        if (fragmentList.isNullOrEmpty()) {
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
            finish()
            return
        }

        when {
            fragmentList[0] is ProvinceSelectorFragment -> {
                (fragmentList[0] as ProvinceSelectorFragment).onBackPressed()
            }
            fragmentList[0] is SuburbSelectorFragment -> {
                (fragmentList[0] as SuburbSelectorFragment).onBackPressed()
            }
            fragmentList[0] is UnsellableItemsFragment -> {
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                finish()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}