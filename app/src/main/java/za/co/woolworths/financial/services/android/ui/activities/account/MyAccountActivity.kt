package za.co.woolworths.financial.services.android.ui.activities.account

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_account_activity.*
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils

class MyAccountActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_OPEN_STATEMENT = 3334
    }

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_account_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()
        if (savedInstanceState == null) {
            addFragment(
                    fragment = MyAccountsFragment(),
                    tag = MyAccountsFragment::class.java.simpleName,
                    containerViewId = R.id.accountContainerFrameLayout)
        }
    }

    fun replaceFragment(fragment: Fragment) {
        replaceFragmentSafely(
                fragment = fragment,
                tag = fragment::class.java.simpleName,
                containerViewId = R.id.accountContainerFrameLayout,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right,
                allowBackStack = true
        )
    }

    private fun actionBar() {
        setSupportActionBar(myAccountToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentByTag(MyAccountsFragment::class.java.simpleName)
        when(requestCode){
            REQUEST_CODE_OPEN_STATEMENT -> {
                finish()
                overridePendingTransition(0,0)
                return
            }
        }
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        with(supportFragmentManager) {
            // Hide store detail if expanded
            val fragment = findFragmentByTag(StoresNearbyFragment1::class.java.simpleName)
            if (fragment is StoresNearbyFragment1) {
                when ((fragment as? StoresNearbyFragment1)?.getSlidingPanelState()) {
                    SlidingUpPanelLayout.PanelState.EXPANDED, SlidingUpPanelLayout.PanelState.ANCHORED -> {
                        (fragment as? StoresNearbyFragment1)?.collapseSlidingPanel()
                        return
                    }
                    else -> {}
                }
            }
            if (backStackEntryCount > 0) {
                popBackStack()
            } else {
                finish()
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }
    }

    fun setToolbarTitle(title: String?) {
        accountToolbarTitle?.text = title
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val fragment = supportFragmentManager.findFragmentByTag(StoresNearbyFragment1::class.java.simpleName)
        (fragment as? StoresNearbyFragment1)?.onRequestPermissionsResult(requestCode, permissions, grantResults)
                ?: (fragment as? ShopFragment)?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun onSignedOut() {
        ScreenManager.presentSSOLogout(this@MyAccountActivity)
    }
}