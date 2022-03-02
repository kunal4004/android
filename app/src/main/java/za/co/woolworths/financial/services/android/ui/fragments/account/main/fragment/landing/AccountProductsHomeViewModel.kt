package za.co.woolworths.financial.services.android.ui.fragments.account.main.fragment.landing

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.usecase.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.usecase.IAccountProductLandingDao
import javax.inject.Inject

@HiltViewModel
class AccountProductsHomeViewModel @Inject constructor(
    bottomSheet: WBottomSheetBehaviour,
    graph: NavigationGraph,
    account: AccountProductLandingDao,
) : ViewModel(),
    IBottomSheetBehaviour by bottomSheet,
    INavigationGraph by graph,
    IAccountProductLandingDao by account



