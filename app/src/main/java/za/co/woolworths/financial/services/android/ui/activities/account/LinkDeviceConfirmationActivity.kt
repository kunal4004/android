package za.co.woolworths.financial.services.android.ui.activities.account

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_link_device_confirmation.*
import za.co.woolworths.financial.services.android.util.Utils

class LinkDeviceConfirmationActivity : AppCompatActivity(), LinkDeviceConfirmationInterface {

    private var linkDeviceNavHost: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_device_confirmation)

        val navHost = supportFragmentManager.findFragmentById(R.id.linkDeviceConfirmationNavHost) as? NavHostFragment
        linkDeviceNavHost = navHost?.navController

        configureToolbar()
        setNavHostStartDestination()
    }

    private fun configureToolbar() {
        setSupportActionBar(linkDeviceConfirmToolbar)
        Utils.updateStatusBarBackground(this)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
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

    override fun onBackPressed() = when (linkDeviceNavHost?.currentDestination?.id) {
        R.id.linkDeviceConfirmationFragment -> {
            finishActivity()
        }
        linkDeviceNavHost?.graph?.startDestination -> {
            finishActivity()
        }
        else -> super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val navHostFragment = supportFragmentManager.fragments[0] as NavHostFragment
        val childFragments = navHostFragment.childFragmentManager.fragments
        for (fragment in childFragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun finishActivity() {
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    private fun setNavHostStartDestination() {
            val graph = linkDeviceNavHost?.graph
            graph?.startDestination = R.id.linkDeviceConfirmationFragment

            graph?.let { linkDeviceNavHost?.setGraph(it, intent.extras) }
    }

    override fun setToolbarTitle(title: String) {
        linkDeviceConfirmToolbarTitle?.text = title
    }

    override fun hideToolbarButton() {
        linkDeviceConfirmToolbarRightButton?.visibility = View.GONE
    }

    override fun showToolbarButton() {
        linkDeviceConfirmToolbarRightButton?.visibility = View.VISIBLE
    }

    override fun hideBackButton() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    override fun showBackButton() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

     override fun getToolbar(): Toolbar {
         return linkDeviceConfirmToolbar
    }
}