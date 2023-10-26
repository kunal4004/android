package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.showErrorDialog
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery.SetUpDeliveryNowDialog
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.WfsBiometricManager
import za.co.woolworths.financial.services.android.ui.wfs.common.contentView
import za.co.woolworths.financial.services.android.ui.wfs.common.state.AppLifeCycleObserver
import za.co.woolworths.financial.services.android.ui.wfs.common.state.LifecycleTransitionType
import za.co.woolworths.financial.services.android.ui.wfs.common.state.BiometricSingleton
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.conditional
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.navigation.AccountLandingEventLauncherImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.navigation.FragmentResultType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.screen.UserAccountsLandingScene
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.Authenticated
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.ManageLoginRegister
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import javax.inject.Inject

@AndroidEntryPoint
class UserAccountsLandingFragment : Fragment() {

    val viewModel: UserAccountLandingViewModel by activityViewModels()
    private var deepLinkParams: JsonObject? = null

    private val mRegisterActivityForResult = BetterActivityResult.registerActivityForResult(this)

    @Inject lateinit var navigation: AccountLandingEventLauncherImpl

    @Inject lateinit var biometricManager: WfsBiometricManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation.onCreatePutC2Id()
        deepLinkParams = viewModel.parseDeepLinkData(arguments)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
    = contentView(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)) {

        OneAppTheme {
            Box (modifier = Modifier
                .background(Color.White)
                .conditional(
                    viewModel.isBiometricScreenEnabled,
                    ifTrue = { blur(30.dp) },
                    ifFalse = null
                )) {
                UserAccountsLandingScene(viewModel, onProductClick = { productGroup ->
                    viewModel.accountProductCardsGroup = productGroup
                    navigation.onProductClicked(
                        productGroup = productGroup,
                        viewModel = viewModel,
                        activityLauncher = mRegisterActivityForResult
                    )
                })
                { view ->
                    navigation.onItemSelectedListener(
                        event = view,
                        viewModel = viewModel,
                        activityLauncher = mRegisterActivityForResult
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveFirebaseDeviceId()
        setupListeners()
        viewLifecycleOwner.lifecycle.addObserver(appLifecycleObserver)
    }

    private fun saveFirebaseDeviceId() {
        viewLifecycleOwner.lifecycleScope.launch {
            navigation.onViewCreatedSaveFirebaseDeviceId()
        }
    }

    private fun setupListeners() {
        setFragmentResultListener(requestKey = userAccountsLandingFragment) { requestKey, bundle ->
            // required when request triggered from other fragments
            with(navigation) {
                when (requestKey) {
                    SetUpDeliveryNowDialog::class.java.simpleName -> {
                        navigation.redirectToCreditCardActivity(viewModel)
                    }

                    userAccountsLandingFragment -> when (bundle.getInt(FragmentResultType.key)) {
                        FragmentResultType.SignOut -> {
                            viewModel.onManageLoginRegister(
                                ManageLoginRegister.SignOut,
                                mRegisterActivityForResult
                            )
                        }
                    }
                }
            }
        }

        with(viewModel) {
            errorResponse.observe(viewLifecycleOwner) { errorResponse ->
                errorResponse?.apply {
                        showErrorDialog(requireActivity() as? AppCompatActivity, errorResponse)
                }
            }

            lifecycleScope.launch {
                isDeeplinkParamsAvailable.collect {isVisible ->
                    if (isVisible) {
                        navigation.navigateToDeepLinkData(
                            deepLinkParams = deepLinkParams,
                            viewModel = viewModel,
                            activityLauncher = mRegisterActivityForResult,
                        )
                        deepLinkParams = null
                    }
                }
            }

            onActivityForResultClicked.observe(viewLifecycleOwner) { click ->
                click?.let { accountProductCardsGroup ->
                    navigation.onProductClicked(productGroup = accountProductCardsGroup, viewModel = this, activityLauncher = mRegisterActivityForResult)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e("enableBiometrics", "onStart")
        enableBiometric()
    }

    private fun enableBiometric() {
        val bottomNavigationActivity = (requireActivity() as? BottomNavigationActivity)
        if (viewModel.wasActivityOpened) {
        if (bottomNavigationActivity?.bottomNavigationById?.currentItem == BottomNavigationActivity.INDEX_ACCOUNT && biometricManager.isBiometricInMyPreferencesEnabled(requireContext())) {
            viewModel.setOnTapActivated()
            viewModel.setBiometricEnabled()
            }
        }
        viewModel.setOnTapActivated()
    }

    override fun onHiddenChanged(isHidden: Boolean) {
        super.onHiddenChanged(isHidden)
        val bottomNavigationActivity = (requireActivity() as? BottomNavigationActivity)
        val bottomNavigationById = bottomNavigationActivity?.bottomNavigationById

        if (isHidden) {
            if (bottomNavigationById?.currentItem == BottomNavigationActivity.INDEX_ACCOUNT) {
                viewModel.setOnTapNotActivated()
                viewModel.setBiometricDisabled()
            }
        }

        if (!isHidden) {
            viewModel.setOnTapActivated()
            enableBiometric()
        }

        with(viewModel) {
            isAccountFragmentVisible(isVisible = !isHidden)
            setUserAuthentication()
            if (!isHidden) {
                navigation.setScreenNameMyAccount()
                navigation.hideToolbar()
                if (isUserAuthenticated.value == Authenticated) {
                    queryAccountLandingService(isApiUpdateForced = !fetchAccountDidLoadOnce)
                }
            }
        }
    }

    private val appLifecycleObserver = AppLifeCycleObserver(biometricSingleton = getBiometricSingleton()) { transitionType ->
        if (transitionType == LifecycleTransitionType.BACKGROUND_TO_FOREGROUND) {
            viewModel.apply {
                if (wasActivityOpened) {
                    setOnTapActivated()
                    setBiometricEnabled()
                }
                setOnTapActivated()
                biometricManager.setupBiometricAuthenticationForAccountLanding(
                    biometricSingleton = getBiometricSingleton(),
                    this@UserAccountsLandingFragment,
                    (requireActivity() as? BottomNavigationActivity),
                    this
                )
            }
        }
    }

    companion object {
        val userAccountsLandingFragment: String = UserAccountsLandingFragment::class.java.simpleName
        const val ACCOUNT_CARD_REQUEST_CODE = 2043
        const val RELOAD_ACCOUNT_RESULT_CODE = 55555
        const val PET_INSURANCE_REQUEST_CODE = 1212
    }

    private fun getBiometricSingleton(): BiometricSingleton? {
       val woolworthsApplication : WoolworthsApplication? = WoolworthsApplication.getInstance()
       return  woolworthsApplication?.biometricSingleton
    }

}

