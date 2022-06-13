package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource

interface ICreditLimitIncrease {
    fun isCliFlowHiddenForProductNotInGoodStanding(): Boolean
    suspend fun queryCliServiceOfferActive(): Flow<CoreDataSource.IOTaskResult<OfferActive>>
}