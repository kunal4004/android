package za.co.woolworths.financial.services.android.ui.activities

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditCardActivationActivityBinding
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardActivationActivity : AppCompatActivity() {

    private lateinit var binding: CreditCardActivationActivityBinding
    var bundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreditCardActivationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)
        bundle = intent.getBundleExtra("bundle")
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
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    private fun loadNavHostFragment() {
        findNavController(R.id.nav_host_fragment)
                .setGraph(
                        R.navigation.nav_graph_credit_card_activation,
                        bundleOf("bundle" to bundle)
                )
    }
}