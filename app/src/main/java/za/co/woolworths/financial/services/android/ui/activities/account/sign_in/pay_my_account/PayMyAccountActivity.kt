package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account

import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pay_my_account_activity.*
import za.co.woolworths.financial.services.android.contracts.IPaymentOptionContract
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl.Companion.ACCOUNT_INFO
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.CreditAndDebitCardPaymentsFragment
import za.co.woolworths.financial.services.android.util.Utils


class PayMyAccountActivity : AppCompatActivity(), IPaymentOptionContract.PayMyAccountView {

    private var navigationHost: NavController? = null
    private var mPayMyAccountPresenterImpl: PayMyAccountPresenterImpl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this)
        setContentView(R.layout.pay_my_account_activity)
        configureToolbar()
        preventStatusBarToBlink()
        setupPresenter()
        setNavHostStartDestination()
    }

    private fun setNavHostStartDestination() {
        navigationHost = findNavController(R.id.payMyAccountNavHostFragmentContainerView)
        val bundle = Bundle()
        bundle.putString(ACCOUNT_INFO, intent?.getStringExtra(ACCOUNT_INFO))
        navigationHost?.graph?.let { graph -> navigationHost?.setGraph(graph, bundle) }
    }

    private fun setupPresenter() {
        mPayMyAccountPresenterImpl = PayMyAccountPresenterImpl(this, PayMyAccountModelImpl())
        mPayMyAccountPresenterImpl?.retrieveAccountBundle(intent)
    }

    private fun preventStatusBarToBlink() {
        val fade = Fade()
        fade.excludeTarget(R.id.payMyAccountToolbar, true)
        fade.excludeTarget(android.R.id.statusBarBackground, true)
        fade.excludeTarget(android.R.id.navigationBarBackground, true)
        window?.enterTransition = fade
        window?.exitTransition = fade
    }

    private fun configureToolbar() {
        setSupportActionBar(payMyAccountToolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    override fun getPayMyAccountPresenter(): PayMyAccountPresenterImpl? {
        return mPayMyAccountPresenterImpl
    }

    override fun configureToolbar(title: String?) {
        super.configureToolbar(title)
        payMyAccountTitleBar?.text = title
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val navHostFragment: NavHostFragment? = supportFragmentManager.fragments.first() as? NavHostFragment
        if (navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            childFragments.forEach { fragment ->
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val navHostFragment: NavHostFragment? = supportFragmentManager.fragments.first() as? NavHostFragment
        if (navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            childFragments.forEach { fragment ->
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onBackPressed() {
        when (currentFragment) {
            is CreditAndDebitCardPaymentsFragment -> {
                finish()
                overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
            }
            else -> navigationHost?.popBackStack()
        }
    }

    val currentFragment: Fragment?
        get() = (supportFragmentManager.fragments.first() as? NavHostFragment)?.childFragmentManager?.findFragmentById(R.id.payMyAccountNavHostFragmentContainerView)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }


    fun displayToolbarDivider(isDividerVisible: Boolean) {
        payMyAccountDivider?.visibility = if (isDividerVisible) VISIBLE else GONE
    }
}