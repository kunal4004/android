package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductLandingActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PMA3DSecureProcessRequestFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.AccountProductsMainFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants.ACCOUNT_PRODUCT_PAYLOAD
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.ui.views.snackbar.OneAppSnackbar
import java.util.*
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class StoreCardActivity : AppCompatActivity() {

    lateinit var binding: AccountProductLandingActivityBinding

    val homeViewModel: AccountProductsHomeViewModel by viewModels()
    val payMyAccountViewModel: PayMyAccountViewModel by viewModels()

    @Inject lateinit var statusBarCompat: SystemBarCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = AccountProductLandingActivityBinding.inflate(layoutInflater)
        homeViewModel.accountData = Utils.jsonStringToObject(intent.extras?.getString(ACCOUNT_PRODUCT_PAYLOAD),Account::class.java) as Account
        setContentView(binding.root)
        statusBarCompat.setLightStatusAndNavigationBar()
        setupView()

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
        OneAppSnackbar.make(binding.rootContainer.rootView, bindString(stringId).toUpperCase(
            Locale.getDefault())).show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val extras = data?.extras
        when (requestCode) {
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
        }
    }
}