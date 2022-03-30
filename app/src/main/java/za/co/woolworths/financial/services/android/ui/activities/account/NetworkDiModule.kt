package za.co.woolworths.financial.services.android.ui.activities.account

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.ITreatmentPlanDataSource
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.TreatmentPlanDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.AccountRemoteService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.IStoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao

@InstallIn(SingletonComponent::class)
@Module
class NetworkDiModule {

    @Provides
    fun provideTreatmentPlanDataSource(): ITreatmentPlanDataSource {
        return TreatmentPlanDataSource()
    }


    @Provides
    fun provideAccountProductLandingDao(): AccountProductLandingDao {
        return AccountProductLandingDao()
    }

    @Provides
    fun provideStoreCardDataSource( accountRemoteService: AccountRemoteService, landingDao: AccountProductLandingDao): IStoreCardDataSource {
        return StoreCardDataSource(accountRemoteService, landingDao)
    }
}