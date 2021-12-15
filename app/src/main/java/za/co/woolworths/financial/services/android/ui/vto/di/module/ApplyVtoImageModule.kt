package za.co.woolworths.financial.services.android.ui.vto.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.ui.vto.data.ApplyVtoImageRepository
import za.co.woolworths.financial.services.android.ui.vto.data.ApplyVtoImageRepositoryImpl


@Module
@InstallIn(ViewModelComponent::class)
abstract class ApplyVtoImageModule {

    @Binds
    abstract fun bindApplyEffect(applyVtoImageRepositoryImpl: ApplyVtoImageRepositoryImpl): ApplyVtoImageRepository

}