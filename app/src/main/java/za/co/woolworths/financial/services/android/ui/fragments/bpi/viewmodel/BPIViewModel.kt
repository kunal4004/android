package za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import za.co.woolworths.financial.services.android.ui.fragments.bpi.helper.NavGraphRouterImpl

class BPIViewModel : ViewModel() {

    var bpiPresenter: BPIOverviewPresenter? = null

    companion object {
        const val externalURL = "http://www.woolworths.co.za/store/fragments/corporate/corporate-index.jsp?content=corporate-content&contentId=cmp208540"
    }

    fun overviewPresenter(argument: Bundle?): BPIOverviewPresenter? {
        bpiPresenter = BPIOverviewPresenter(
            BPIOverviewOverviewImpl(argument),
            BPISubmitClaimImpl(),
            NavGraphRouterImpl(),
            BPIDefaultLabelListImpl()
        )
        return bpiPresenter
    }

}