package za.co.woolworths.financial.services.android.ui.activities.click_and_collect

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.edit_delivery_location_activity.*
import za.co.woolworths.financial.services.android.util.Utils

class ProvinceAndSuburbSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.province_and_suburb_selection_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()
        loadNavHostFragment()
    }

    private fun actionBar() {
        setSupportActionBar(toolbar)
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
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    private fun loadNavHostFragment() {
        val navHostFragment = nav_host_fragment as NavHostFragment
        val graph = navHostFragment.navController.navInflater.inflate(R.navigation.province_and_suburb_selection_nav_host)
        graph.startDestination = if (intent.hasExtra("ProvinceList")) R.id.provinceSelectorFragment else R.id.suburbSelectorFragment
        findNavController(R.id.nav_host_fragment).graph = graph
    }
}