package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.presentation.common.HeaderView
import za.co.woolworths.financial.services.android.presentation.common.HeaderViewState
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.OrderAgainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderAgainScreen(
    viewModel: OrderAgainViewModel,
    onEvent: () -> Unit
) {

    Scaffold(
        topBar = {
            HeaderView(
                headerViewState = HeaderViewState.HeaderStateType1(
                    title = stringResource(id = R.string.order_again)
                )
            ) {
                
            }
        },
    ) {
        OrderAgainStatelessScreen(Modifier.padding(it))
    }

}

@Composable
private fun OrderAgainStatelessScreen(modifier: Modifier = Modifier) {

}
