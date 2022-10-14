package za.co.woolworths.financial.services.android.ui.activities.account

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.ContactUsDataSource
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.IContactUsDataSource
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
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
        wfsApiService: WfsApiService, landingDao: AccountProductLandingDao,
        manageCard: ManageCardFunctionalRequirementImpl
    ): IStoreCardDataSource {
        return StoreCardDataSource(wfsApiService, landingDao, manageCard)
    }

    @Provides
    fun provideCreditLimitIncreaseDataSource(
        wfsApiService: WfsApiService,
        landingDao: AccountProductLandingDao,
    ): ICreditLimitIncrease {
        return CreditLimitIncreaseDataSource(wfsApiService, landingDao)
    }

    @Provides
    fun provideContactUsDataSource(): IContactUsDataSource {
        return ContactUsDataSource()
    }
}