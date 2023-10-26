package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.Authenticated
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.NotAuthenticated
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OnAccountItemClickListener
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel

@Composable
fun UserAccountsLandingScene(
    viewModel: UserAccountLandingViewModel,
    onProductClick : (AccountProductCardsGroup) -> Unit,
    onClickEvent: (OnAccountItemClickListener) -> Unit) {
        val isUserAuthenticated by remember { viewModel.isUserAuthenticated }
        when (isUserAuthenticated) {
            Authenticated -> {
                SignedInScreen(
                    viewModel = viewModel,
                    onProductClick =  onProductClick,
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

