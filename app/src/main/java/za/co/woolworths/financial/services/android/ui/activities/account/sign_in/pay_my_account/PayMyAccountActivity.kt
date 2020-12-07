package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pay_my_account_activity.*
import za.co.woolworths.financial.services.android.contracts.IPaymentOptionContract
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl.Companion.GET_CARD_RESPONSE
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl.Companion.IS_DONE_BUTTON_ENABLED
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl.Companion.SCREEN_TYPE
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.CreditAndDebitCardPaymentsFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment.Companion.PMA_UPDATE_CARD_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMAManageCardFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType

class PayMyAccountActivity : AppCompatActivity(), IPaymentOptionContract.PayMyAccountView {

    private var navigationHost: NavController? = null
    private var mPayMyAccountPresenterImpl: PayMyAccountPresenterImpl? = null
    private val payMyAccountViewModel: PayMyAccountViewModel by viewModels()

    companion object {
        const val PAY_MY_ACCOUNT_REQUEST_CODE = 8003
        const val PAYMENT_DETAIL_CARD_UPDATE = "PAYMENT_DETAIL_CARD_UPDATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this)
        setContentView(R.layout.pay_my_account_activity)

        val payMyAccountFragmentContainer = supportFragmentManager.findFragmentById(R.id.payMyAccountNavHostFragmentContainerView) as? NavHostFragment
        navigationHost = payMyAccountFragmentContainer?.navController

        configureToolbar()
        setupPresenter()
        setNavHostStartDestination()
    }

    private fun setNavHostStartDestination() {
        intent?.extras?.apply {
            val bundle = Bundle()
            bundle.putString(GET_CARD_RESPONSE, getString(GET_CARD_RESPONSE,""))
            bundle.putBoolean(IS_DONE_BUTTON_ENABLED, getBoolean(IS_DONE_BUTTON_ENABLED, false))
            val graph = navigationHost?.graph
            graph?.startDestination = when (getSerializable(SCREEN_TYPE) as? PayMyAccountStartDestinationType
                    ?: PayMyAccountStartDestinationType.CREATE_USER) {
                PayMyAccountStartDestinationType.CREATE_USER -> R.id.creditAndDebitCardPaymentsFragment
                PayMyAccountStartDestinationType.MANAGE_CARD -> R.id.manageCardFragment
                PayMyAccountStartDestinationType.PAYMENT_AMOUNT -> R.id.enterPaymentAmountFragment
                PayMyAccountStartDestinationType.SECURE_3D -> R.id.pmaProcessRequestFragment
                else -> R.id.addNewPayUCardFragment
            }

            graph?.let { navigationHost?.setGraph(it, bundle) }
        }
    }

    private fun setupPresenter() {
        val paymentCardDetailUpdate = intent?.extras?.getString(PAYMENT_DETAIL_CARD_UPDATE, "")
        paymentCardDetailUpdate?.let { payMyAccountViewModel.setPMACardInfo(it) }
        mPayMyAccountPresenterImpl = PayMyAccountPresenterImpl(this, PayMyAccountModelImpl())
        mPayMyAccountPresenterImpl?.retrieveAccountBundle(payMyAccountViewModel.getAccountWithApplyNowState())
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

    override fun getPayMyAccountPresenter(): PayMyAccountPresenterImpl? = mPayMyAccountPresenterImpl

    override fun configureToolbar(title: String?) {
        super.configureToolbar(title)
        payMyAccountTitleBar?.text = title
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
        // disable back button until delete card call is completed
        if (!payMyAccountViewModel.isDeleteCardListEmpty()) return

        when (currentFragment) {
            is PMAManageCardFragment,
            is CreditAndDebitCardPaymentsFragment,
            is PMA3DSecureProcessRequestFragment -> {
                closeActivity()
            }
            else -> {
                when (navigationHost?.graph?.startDestination) {
                    R.id.addNewPayUCardFragment,
                    R.id.enterPaymentAmountFragment,
                    R.id.pmaProcessRequestFragment -> {
                        closeActivity()
                    }
                    else -> navigationHost?.popBackStack()
                }
            }
        }
    }

    private fun closeActivity() {
        setResult(RESULT_OK, Intent().putExtra(PAYMENT_DETAIL_CARD_UPDATE, payMyAccountViewModel.getCardDetailInStringFormat()))
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    val currentFragment: Fragment?
        get() = (supportFragmentManager.fragments.first()
                as? NavHostFragment)?.childFragmentManager?.findFragmentById(R.id.payMyAccountNavHostFragmentContainerView)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun displayToolbarDivider(isDividerVisible: Boolean) {
        payMyAccountDivider?.visibility = if (isDividerVisible) VISIBLE else GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val extras = data?.extras
        when (requestCode) {
            PAY_MY_ACCOUNT_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK, PMA_UPDATE_CARD_RESULT_CODE -> {
                        extras?.getString(PAYMENT_DETAIL_CARD_UPDATE,"")?.apply {
                            payMyAccountViewModel.setPMACardInfo(this)
                        }
                    }

                    PMA3DSecureProcessRequestFragment.PMA_TRANSACTION_COMPLETED_RESULT_CODE -> {
                        extras?.getString(PAYMENT_DETAIL_CARD_UPDATE,"")?.apply {
                            payMyAccountViewModel.setPMACardInfo(this)
                        }
                        payMyAccountViewModel.queryPaymentMethod.value = true
                    }

                    else -> {
                        val navHostFragment: NavHostFragment
                        ? = supportFragmentManager.fragments.first() as? NavHostFragment
                        if (navHostFragment != null) {
                            val childFragments = navHostFragment.childFragmentManager.fragments
                            childFragments.forEach { fragment ->
                                fragment.onActivityResult(requestCode, resultCode, data)
                            }
                        }
                    }
                }
            }
        }
    }
}