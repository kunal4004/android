package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductLandingActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.ACTIVATE_VIRTUAL_TEMP_CARD_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeUnfreezeCardItemFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setToast
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.AccountProductsMainFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants.ACCOUNT_PRODUCT_PAYLOAD
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.ActivityIntentNavigationManager
import za.co.woolworths.financial.services.android.util.voc.VoiceOfCustomerManager
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class StoreCardActivity : AppCompatActivity() {

    lateinit var binding: AccountProductLandingActivityBinding
    val homeViewModel: AccountProductsHomeViewModel by viewModels()
    val payMyAccountViewModel: PayMyAccountViewModel by viewModels()
    val cardFreezeViewModel: TemporaryFreezeCardViewModel by viewModels()
    val viewModel: MyAccountsRemoteApiViewModel by viewModels()

    @Inject lateinit var statusBarCompat: SystemBarCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = AccountProductLandingActivityBinding.inflate(layoutInflater)
        homeViewModel.accountData = Utils.jsonStringToObject(intent.extras?.getString(ACCOUNT_PRODUCT_PAYLOAD),Account::class.java) as? Account
        setContentView(binding.root)
        statusBarCompat.setLightStatusAndNavigationBar()
        homeViewModel.setDeepLinkParams(intent?.extras)
        setupView()
        setObservers()
    }

    private fun setObservers() {
        lifecycleScope.launch{
            cardFreezeViewModel.showToastMessageOnStoreCardFreeze.observe(this@StoreCardActivity) { result ->
                showToast(result)
            }
        }
    }

    private fun setupView() {
        setupGraph(
            containerId = R.id.accountProductLandingFragmentContainerView,
            graphResId = R.navigation.nav_account_product_landing,
            startDestination = R.id.accountProductsMainFragment,
            startDestinationArgs = intent.extras
        )
    }

    private fun getMainFragment() = supportFragmentManager.findFragmentById(R.id.accountProductLandingFragmentContainerView)?.childFragmentManager?.primaryNavigationFragment as? AccountProductsMainFragment

    fun landingNavController(): NavController? {
        val fragment = getMainFragment()
        val navHost = fragment?.getChildNavHost()
        return navHost?.navController
    }

    fun getToolbarHelper() = getMainFragment()?.mToolbarContainer

    fun getBackIcon() = getToolbarHelper()?.getBackIcon()

    fun showToast(@StringRes stringId : Int) {
        setToast(binding.rootContainer, stringId)
    }

    /**
     * On activity result
     * @param requestCode
     * @param resultCode
     * @param data
     * TODO:: onActivityResult, @Deprecated("Deprecated in Java"), Replace with registerForActivityResult()
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val extras = data?.extras
        when (requestCode) {

            TemporaryFreezeUnfreezeCardItemFragment.DEVICE_SECURITY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    cardFreezeViewModel.mDeviceSecurityFlagState.fillRevertSwitcher()
                }
            }

            PayMyAccountActivity.PAY_MY_ACCOUNT_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK, PMA3DSecureProcessRequestFragment.PMA_UPDATE_CARD_RESULT_CODE -> {
                        extras?.getString(PayMyAccountActivity.PAYMENT_DETAIL_CARD_UPDATE)?.apply {
                            payMyAccountViewModel.setPMACardInfo(this)
                        }
                    }


                    // on back to my account pressed (R.string.back_to_my_account_button)
                    PMA3DSecureProcessRequestFragment.PMA_TRANSACTION_COMPLETED_RESULT_CODE -> {
                        extras?.getString(PayMyAccountActivity.PAYMENT_DETAIL_CARD_UPDATE)?.apply {
                            payMyAccountViewModel.setPMACardInfo(this)
                        }
                    }
                }
            }

            AccountsOptionFragment.REQUEST_ELITEPLAN -> {
                //elitePlanModel is the model extracted from callback url parameters
                if (extras?.containsKey(AccountSignedInPresenterImpl.ELITE_PLAN_MODEL) == true) {
                    val elitePlanModel =
                        extras.getParcelable(AccountSignedInPresenterImpl.ELITE_PLAN_MODEL) as? Parcelable
                    ActivityIntentNavigationManager.presentPayMyAccountActivity(
                        this@StoreCardActivity,
                        payMyAccountViewModel.getCardDetail(),
                        PayMyAccountStartDestinationType.CREATE_USER,
                        true,
                        elitePlanModel
                    )
                } else {
                    onTreatmentPlanStatusUpdateRequired()
                }
            }

            AccountsOptionFragment.REQUEST_GET_PAYMENT_PLAN -> {
                if (resultCode == Activity.RESULT_OK) {
                    onTreatmentPlanStatusUpdateRequired()
                }
            }

        }

        when (resultCode) {
            ACTIVATE_VIRTUAL_TEMP_CARD_RESULT_CODE -> {
                //ICR Journey success and When Get replacement card email confirmation is success and result ok
                VoiceOfCustomerManager.pendingTriggerEvent = VocTriggerEvent.MYACCOUNTS_ICR_LINK_CONFIRM
                requestGetStoreCardCards()
            }
        }
    }

    private fun onTreatmentPlanStatusUpdateRequired() {
        lifecycleScope.launch {
            homeViewModel.requestAccountsCollectionsCheckEligibility(false)
        }
    }
    private fun requestGetStoreCardCards() {
        lifecycleScope.launch {
            viewModel.requestGetStoreCardCards()
        }
    }

    companion object {
        var SHOW_TEMPORARY_FREEZE_DIALOG = false
        var FREEZE_CARD_DETAIL = false
        var SHOW_BLOCK_CARD_SCREEN = false
        var BLOCK_CARD_DETAIL = false
        var SHOW_PAY_WITH_CARD_SCREEN = false
        var PAY_WITH_CARD_DETAIL = false
        var SHOW_GET_REPLACEMENT_CARD_SCREEN = false
        var GET_REPLACEMENT_CARD_DETAIL = false
        var SHOW_ACTIVATE_VIRTUAL_CARD_SCREEN = false
        var ACTIVATE_VIRTUAL_CARD_DETAIL = false
    }
}