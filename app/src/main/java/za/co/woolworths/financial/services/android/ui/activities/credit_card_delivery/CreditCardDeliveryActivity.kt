package za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditCardDeliveryActivityBinding
import za.co.woolworths.financial.services.android.analytic.FirebaseCreditCardDeliveryEvent
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardDeliveryStatus
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.StatusResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.extension.asEnumOrDefault
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryActivity : AppCompatActivity() {

    private lateinit var binding: CreditCardDeliveryActivityBinding
    var bundle: Bundle? = null
    var accountBinNumber: String? = null
    var statusResponse: StatusResponse? = null
    var setUpDeliveryNowClicked: Boolean = false;
    var mFirebaseCreditCardDeliveryEvent: FirebaseCreditCardDeliveryEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreditCardDeliveryActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this, R.color.grey_bg)
        bundle = intent.getBundleExtra(BundleKeysConstants.BUNDLE)
        bundle?.apply {
            statusResponse = getParcelable(BundleKeysConstants.STATUS_RESPONSE) as StatusResponse?
            accountBinNumber = getString(BundleKeysConstants.ACCOUNTBI_NNUMBER)
            setUpDeliveryNowClicked = getBoolean("setUpDeliveryNowClicked", false);
            val applyNowState: ApplyNowState? = getSerializable(AccountSignedInPresenterImpl.APPLY_NOW_STATE) as? ApplyNowState
            if (applyNowState != null)
                mFirebaseCreditCardDeliveryEvent = FirebaseCreditCardDeliveryEvent(applyNowState = applyNowState, activity = this@CreditCardDeliveryActivity)
        }
        actionBar()
        loadNavHostFragment()
    }

    enum class DeliveryStatus(val value: Int) { CANCEL_DELIVERY(0), EDIT_ADDRESS(1) }

    private fun actionBar() {
        binding.toolbar.contentDescription = bindString(R.string.navigate_up)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    fun setToolbarTitle(title: String?) {
        binding.toolbarText?.text = title
    }

    fun hideToolbar() {
        binding.toolbar?.visibility = View.GONE
    }

    fun changeToolbarBackground(color: Int) {
        with(binding) {
            toolbar?.visibility = View.VISIBLE
            toolbar.setBackgroundColor(bindColor(color))
            Utils.updateStatusBarBackground(this@CreditCardDeliveryActivity, color)
        }
    }

    private fun loadNavHostFragment() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val graph = navHostFragment.navController.navInflater.inflate(R.navigation.nav_graph_credit_card_delivery)

        if (setUpDeliveryNowClicked)
            graph.startDestination = R.id.creditCardDeliveryRecipientDetailsFragment
        else if (statusResponse?.deliveryStatus?.statusDescription?.asEnumOrDefault(CreditCardDeliveryStatus.DEFAULT) == CreditCardDeliveryStatus.CARD_RECEIVED)
            graph.startDestination = R.id.creditCardDeliveryBoardingFragment
        else
            graph.startDestination = R.id.creditCardDeliveryStatusFragment
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