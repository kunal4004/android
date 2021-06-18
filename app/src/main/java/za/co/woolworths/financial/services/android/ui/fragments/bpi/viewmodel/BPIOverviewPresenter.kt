package za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel

import za.co.woolworths.financial.services.android.ui.fragments.bpi.contract.BPIOverviewInterface
import za.co.woolworths.financial.services.android.ui.fragments.bpi.contract.BPISubmitClaimInterface
import za.co.woolworths.financial.services.android.ui.fragments.bpi.helper.NavGraphRouterImpl
import za.co.woolworths.financial.services.android.ui.fragments.bpi.contract.NavigationGraphRouterInterface

class BPIOverviewPresenter(
    private val overview: BPIOverviewInterface,
    private val submitClaim: BPISubmitClaimInterface,
    private val route: NavGraphRouterImpl,
) : BPIOverviewInterface by overview,
    BPISubmitClaimInterface by submitClaim,
    NavigationGraphRouterInterface by route