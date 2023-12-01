package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Repository.DyReportEventRepository
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Repository.DyReportEventRepositoryImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class DyReportEventModule {
    @Binds
    abstract fun bindDyReportEventModule(dyReportEventRepositoryImpl: DyReportEventRepositoryImpl): DyReportEventRepository
}