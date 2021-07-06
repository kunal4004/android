package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_checkout.*
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.ProvinceSelectorFragment
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.SuburbSelectorFragment


/**
 * Created by Kunal Uttarwar on 26/05/21.
 */
class CheckoutActivity : AppCompatActivity() {

    var navHostFrag = NavHostFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        setActionBar()
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
        if (true)
            graph.startDestination = R.id.CheckoutAddAddressNewUserFragment
        else
            graph.startDestination = R.id.CheckoutAddAddressReturningUserFragment
        findNavController(R.id.navHostFragment).graph = graph
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> onBackPressed()
            android.R.id.home -> onBackPressed()
        }
        return false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val fragmentList: MutableList<androidx.fragment.app.Fragment> =
            navHostFrag.childFragmentManager.fragments
        if (fragmentList.size > 0 && fragmentList[0] is ProvinceSelectorFragment) {
            (fragmentList[0] as ProvinceSelectorFragment).onBackPressed()
        }
        if (fragmentList.size > 0 && fragmentList[0] is SuburbSelectorFragment) {
            (fragmentList[0] as SuburbSelectorFragment).onBackPressed()
        }
    }
}