package za.co.woolworths.financial.services.android.ui.fragments.account.main.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.IBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.WBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.BalanceProtectionInsuranceImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IAccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IBalanceProtectionInsurance
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IStoreCardNavigator
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.StoreCardNavigator
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.ManageCardFunctionalRequirementImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.IProductLandingRouter
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import javax.inject.Singleton

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @Singleton
    @Provides
    fun provideProductLandingRouterImpl(
        accountOptions: AccountOptionsImpl,
        manageCardFunctionalRequirementImpl: ManageCardFunctionalRequirementImpl
    ): IProductLandingRouter =
        ProductLandingRouterImpl(accountOptions, manageCardFunctionalRequirementImpl)

    @Provides
    fun provideBottomSheetBehaviour(
        @ApplicationContext context: Context,
        accountDao: AccountProductLandingDao
    ): IBottomSheetBehaviour =
        WBottomSheetBehaviour(context, accountDao)

    @Provides
    fun provideAccountProductLiveEvent(): IAccountProductLandingDao =
        AccountProductLandingDao()

    @Provides
    fun provideBalanceProtectionInsurance(product: AccountProductLandingDao?): IBalanceProtectionInsurance =
        BalanceProtectionInsuranceImpl(product)

    @Provides
    fun provideStoreCardNavigator(): IStoreCardNavigator = StoreCardNavigator()
}