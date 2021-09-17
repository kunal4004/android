package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_checkout.*
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment.Companion.SAVED_ADDRESS_KEY
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment.Companion.baseFragBundle
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.ProvinceSelectorFragment
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.SuburbSelectorFragment
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils


/**
 * Created by Kunal Uttarwar on 26/05/21.
 */
class CheckoutActivity : AppCompatActivity(), View.OnClickListener {

    private var navHostFrag = NavHostFragment()
    var savedAddressResponse: SavedAddressResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        setActionBar()
        intent?.extras?.apply {
            savedAddressResponse = getSerializable(SAVED_ADDRESS_KEY) as? SavedAddressResponse
            baseFragBundle?.putString(
                SAVED_ADDRESS_KEY,
                Utils.toJson(savedAddressResponse)
            )
        }
        loadNavHostFragment()
    }

    fun setActionBar() {
        toolbar?.visibility = View.VISIBLE
        setSupportActionBar(toolbar)
        KotlinUtils.setTransparentStatusBar(this)
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

    fun showTitleWithCrossButton(titleText: String) {
        toolbar?.visibility = View.VISIBLE
        setSupportActionBar(toolbar)
        btnClose?.visibility = View.VISIBLE
        btnClose.setOnClickListener(this)
        toolbarText.text = titleText
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
        }
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
        findNavController(R.id.navHostFragment).setGraph(graph, baseFragBundle)
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

        //in Navigation component if Back stack entry count is 0 means it has last fragment presented.
        // if > 0 means others are in backstack but fragment list size will always be 1
        if (fragmentList.isNullOrEmpty() || navHostFrag.childFragmentManager.backStackEntryCount == 0) {
            finish()
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
            return
        }

        when (fragmentList[0]) {
            is ProvinceSelectorFragment -> {
                (fragmentList[0] as ProvinceSelectorFragment).onBackPressed()
            }
            is SuburbSelectorFragment -> {
                (fragmentList[0] as SuburbSelectorFragment).onBackPressed()
            }
            is UnsellableItemsFragment, is CheckoutAddAddressReturningUserFragment -> {
                finish()
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnClose -> {
                onBackPressed()
            }
        }
    }
}