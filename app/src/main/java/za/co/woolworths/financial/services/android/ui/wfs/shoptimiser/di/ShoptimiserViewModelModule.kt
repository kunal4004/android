package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.datasource.IUserAccountRemote
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.datasource.UserAccountRemoteDataSource
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.repository.MyAccountsLandingRemoteService
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.repository.MyAccountsLandingRemoteServiceImpl
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.WfsShopOptimiserProduct
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.ShopOptimiserShopOptimiserProductImpl
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.IManageBnpLConfig
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.IManageShopOptimiserCalculation
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.IManageShopOptimiserSQLite
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.IRetailBNPL
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.ManageBnplConfigImpl
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.ManageShopOptimiserCalculationImpl
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.ManageShopOptimiserSQLiteImpl
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.RetailBNPLImpl
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.WfsShopOptimiserProductDetailsBuilder
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller.WfsShopOptimiserProductDetailsBuilderImpl
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.pdp.ShoptimiserProductDetailPage
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.pdp.ShoptimiserProductDetailPageImpl

/**
 * Dagger Hilt Module for providing dependencies used in ShoptimiserViewModel.
 * This module binds implementations of various interfaces required by the ViewModel.
 * @see Binds: Used to bind concrete implementations to interface types.
 * @see InstallIn: Specifies that this module is installed in the ViewModelComponent.
 */
@Module
@InstallIn(ViewModelComponent::class)
abstract class ShoptimiserViewModelModule {
    // Bind UserAccountRemoteDataSource to IUserAccountRemote
    @Binds
    abstract fun provideUserAccountRemoteDataSource(remote: UserAccountRemoteDataSource): IUserAccountRemote

    // Bind MyAccountsLandingRemoteServiceImpl to MyAccountsLandingRemoteService
    @Binds
    abstract fun provideMessageProducer(myAccountsImpl: MyAccountsLandingRemoteServiceImpl): MyAccountsLandingRemoteService

    // Bind ShopOptimiserShopOptimiserProductImpl to WfsShopOptimiserProduct
    @Binds
    abstract fun provideDisplayWFSProducts(displayWFSProducts: ShopOptimiserShopOptimiserProductImpl): WfsShopOptimiserProduct

    // Bind WfsShopOptimiserProductDetailsBuilderImpl to WfsShopOptimiserProductDetailsBuilder
    @Binds
    abstract fun provideDisplayWFSProductsDetails(displayWFSProducts: WfsShopOptimiserProductDetailsBuilderImpl): WfsShopOptimiserProductDetailsBuilder

    // Bind ManageBnplConfigImpl to IManageBnpLConfig
    @Binds
    abstract fun provideManageBnplConfig(manageBnplConfig: ManageBnplConfigImpl): IManageBnpLConfig

    // Bind ManageShopOptimiserSQLiteImpl to IManageShopOptimiserSQLite
    @Binds
    abstract fun provideTimestamp(manageWFSTimestamp: ManageShopOptimiserSQLiteImpl): IManageShopOptimiserSQLite

    // Bind ManageShopOptimiserCalculationImpl to IManageShopOptimiserCalculation
    @Binds
    abstract fun provideManageShopOptimiserCalculation(calculation: ManageShopOptimiserCalculationImpl): IManageShopOptimiserCalculation

    // Bind RetailBNPLImpl to IRetailBNPL
    @Binds
    abstract fun provideRetailBNPL(calculation: RetailBNPLImpl): IRetailBNPL

    @Binds
    abstract fun provideShoptimiserProductDetailPage(calculation: ShoptimiserProductDetailPageImpl): ShoptimiserProductDetailPage
}

