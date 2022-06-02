package za.co.woolworths.financial.services.android.ui.fragments.account.main.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.IBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.WBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.INavigationGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.NavigationGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.BalanceProtectionInsuranceImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IAccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IBalanceProtectionInsurance
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.IStoreCardNavigator
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.StoreCardNavigator
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.IProductLandingRouter
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @Provides
    fun provideProductLandingRouterImpl(accountOptions: AccountOptionsImpl): IProductLandingRouter =
        ProductLandingRouterImpl(accountOptions)

    @Provides
    fun provideNavigationGraph(): INavigationGraph = NavigationGraph()

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