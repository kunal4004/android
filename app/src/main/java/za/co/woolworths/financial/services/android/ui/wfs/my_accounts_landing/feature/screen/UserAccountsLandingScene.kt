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
    onProductClick: (AccountProductCardsGroup) -> Unit,
    onClickEvent: (OnAccountItemClickListener) -> Unit
) {
    val isUserAuthenticated by remember { viewModel.isUserAuthenticated }

    when (isUserAuthenticated) {
        Authenticated -> {

            SignedInScreen(
                viewModel = viewModel,
                onProductClick = onProductClick,
                onClick = onClickEvent
            )
        }

        NotAuthenticated -> {
            SignedOutScreen(
                viewModel = viewModel,
                onClick = onClickEvent
            )
        }
    }
}

