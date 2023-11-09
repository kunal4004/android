package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.repository.DyChooseVariationRepository
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.repository.DyChooseVariationRepositoryImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class DyChooseVariationModule {

    @Binds
    abstract fun bindDyChooseVariationModule(dyChooseVariationRepositoryImpl: DyChooseVariationRepositoryImpl): DyChooseVariationRepository
}