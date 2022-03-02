package za.co.woolworths.financial.services.android.ui.activities.account

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.ITreatmentPlanDataSource
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.TreatmentPlanDataSource

@InstallIn(SingletonComponent::class)
@Module
class NetworkDiModule {

    @Provides
    fun provideTreatmentPlanDataSource(): ITreatmentPlanDataSource {
        return TreatmentPlanDataSource()
    }
}