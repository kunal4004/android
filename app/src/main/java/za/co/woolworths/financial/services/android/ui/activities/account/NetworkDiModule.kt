package za.co.woolworths.financial.services.android.ui.activities.account

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.AccountRemoteService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.IStoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase.CreditLimitIncreaseDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase.ICreditLimitIncrease
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.ManageCardFunctionalRequirementImpl

@InstallIn(ViewModelComponent::class)
@Module
class NetworkDiModule {
    @Provides
    fun provideManageCard(accountProductLandingDao: AccountProductLandingDao): ManageCardFunctionalRequirementImpl {
        return ManageCardFunctionalRequirementImpl(accountProductLandingDao)
    }

    @Provides
    fun provideStoreCardDataSource(
        accountRemoteService: AccountRemoteService, landingDao: AccountProductLandingDao,
        manageCard: ManageCardFunctionalRequirementImpl
    ): IStoreCardDataSource {
        return StoreCardDataSource(accountRemoteService, landingDao, manageCard)
    }

    @Provides
    fun provideCreditLimitIncreaseDataSource(
        accountRemoteService: AccountRemoteService,
        landingDao: AccountProductLandingDao,
    ): ICreditLimitIncrease {
        return CreditLimitIncreaseDataSource(accountRemoteService, landingDao)
    }

}