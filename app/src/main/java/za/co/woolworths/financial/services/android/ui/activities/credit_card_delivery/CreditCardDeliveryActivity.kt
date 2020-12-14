package za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_activity.*
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardDeliveryStatus
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.StatusResponse
import za.co.woolworths.financial.services.android.ui.extension.asEnumOrDefault
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryActivity : AppCompatActivity() {

    var bundle: Bundle? = null
    var statusResponse: StatusResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.credit_card_delivery_activity)
        Utils.updateStatusBarBackground(this, R.color.grey_bg)
        bundle = intent.getBundleExtra("bundle")
        bundle?.apply {
            statusResponse = Utils.jsonStringToObject(getString("StatusResponse"), StatusResponse::class.java) as StatusResponse?
        }
        actionBar()
        loadNavHostFragment()
    }

    enum class DeliveryStatus(val value: Int) { CANCEL_DELIVERY(0), EDIT_ADDRESS(1) }

    private fun actionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun setToolbarTitle(title: String?) {
        toolbarText?.text = title
    }

    fun hideToolbar() {
        toolbar?.visibility = View.GONE
    }

    fun changeToolbarBackground(color: Int) {
        toolbar?.visibility = View.VISIBLE
        toolbar.setBackgroundColor(bindColor(color))
        Utils.updateStatusBarBackground(this, color)
    }

    private fun loadNavHostFragment() {
        val navHostFragment = nav_host_fragment as NavHostFragment
        val graph = navHostFragment.navController.navInflater.inflate(R.navigation.nav_graph_credit_card_delivery)

        graph.startDestination = if (statusResponse?.deliveryStatus?.statusDescription?.asEnumOrDefault(CreditCardDeliveryStatus.DEFAULT) == CreditCardDeliveryStatus.CARD_RECEIVED) R.id.creditCardDeliveryBoardingFragment else R.id.creditCardDeliveryStatusFragment
        findNavController(R.id.nav_host_fragment)
                .setGraph(
                        graph,
                        bundleOf("bundle" to bundle)
                )
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
}