package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collectLatest
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.mypreferences.MyPreferencesFragment
import za.co.woolworths.financial.services.android.ui.wfs.common.state.LifecycleTransitionType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.findActivity
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.Authenticated
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.NotAuthenticated
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OnAccountItemClickListener
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.util.AuthenticateUtils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

@Composable
fun UserAccountsLandingScene(
    viewModel: UserAccountLandingViewModel,
    onProductClick : (AccountProductCardsGroup) -> Unit,
    onClickEvent: (OnAccountItemClickListener) -> Unit) {
    val isUserAuthenticated by remember { viewModel.isUserAuthenticated }
    val biometricAuthenticationState by  viewModel.securityTransitionType.collectAsState(LifecycleTransitionType.FOREGROUND)
    val context= LocalContext.current
    val activity = context.findActivity()

    when (isUserAuthenticated) {
        Authenticated -> {
            if (biometricAuthenticationState == LifecycleTransitionType.FOREGROUND) {
                SignedInScreen(
                    viewModel = viewModel,
                    onProductClick = onProductClick,
                    onClick = onClickEvent
                    )
                }else {
                    LaunchedEffect(true){
                    if (AuthenticateUtils.getInstance(activity).isBiometricAuthenticationRequired) {
                        try {
                            AuthenticateUtils.getInstance(activity)
                                .startAuthenticateApp(BottomNavigationActivity.LOCK_REQUEST_CODE_ACCOUNTS)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                                activity?.startActivityForResult(
                                    intent,
                                    MyPreferencesFragment.SECURITY_SETTING_REQUEST_CODE
                                )
                            } catch (ex: Exception) {
                                FirebaseManager.logException(ex)
                            }
                        }
                    }
                    }
                }
            }

            NotAuthenticated -> {
                SignedOutScreen(
                    viewModel = viewModel,
                    onClick = onClickEvent
                )
            }
        }
}

