package za.co.woolworths.financial.services.android.ui.activities.card

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.select_store_activity.*
import za.co.woolworths.financial.services.android.util.Utils

class SelectStoreActivity : AppCompatActivity() {

    companion object {
        const val STORE_DETAILS = "STORE_DETAILS"
    }

    private var navController: NavController? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Clear the Activity's bundle of the subsidiary fragments' bundles.
        outState.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this)
        setContentView(R.layout.select_store_activity)
        setActionBar()
        setNavHostStartDestination()
    }

    private fun setActionBar() {
        setSupportActionBar(vtcReplacementToolbar)
        val mActionBar = supportActionBar
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true)
            mActionBar.setDisplayShowTitleEnabled(false)
            mActionBar.setDisplayUseLogoEnabled(false)
            mActionBar.setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    private fun setNavHostStartDestination() {
        val replacementCardNavHost = supportFragmentManager.findFragmentById(R.id.replacementCardNavHost) as? NavHostFragment
        navController = replacementCardNavHost?.navController
        val graph = replacementCardNavHost?.navController?.graph
        graph?.startDestination = R.id.getReplacementCardFragment
        graph?.let { replacementCardNavHost?.navController?.setGraph(it, if (intent != null) intent.extras else null) }
    }

    override fun onBackPressed() {

        when (navController?.currentDestination?.id) {
            navController?.graph?.startDestination, R.id.getReplacementCardFragment -> {
                finish()
            }
            else -> navController?.navigateUp()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val replacementCardNavHost = supportFragmentManager.findFragmentById(R.id.replacementCardNavHost) as NavHostFragment?
        replacementCardNavHost?.childFragmentManager?.let{
            if (it.backStackEntryCount < 1) {
                return
            }
            val fragment = it.fragments?.get(0)
            fragment?.onActivityResult(requestCode, resultCode, data)
        }
    }

}