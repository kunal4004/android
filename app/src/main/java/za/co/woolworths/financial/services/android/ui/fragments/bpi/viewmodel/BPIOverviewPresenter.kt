package za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel

import za.co.woolworths.financial.services.android.ui.fragments.bpi.contract.*

class BPIOverviewPresenter(
    private val overview: BPIOverviewInterface,
    private val submitClaim: BPISubmitClaimInterface,
    private val route: NavigationGraphRouterInterface,
    private val bpiLabel : BPIDefaultLabelInterface
) : BPIOverviewInterface by overview,
    BPISubmitClaimInterface by submitClaim,
    NavigationGraphRouterInterface by route,
    BPIDefaultLabelInterface by bpiLabel


