package za.co.woolworths.financial.services.android.ui.vto.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.ui.vto.data.LiveCameraRepository
import za.co.woolworths.financial.services.android.ui.vto.data.LiveCameraRepositoryImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class VtoLiveCameraModule {

    @Binds
    abstract fun bindLiveCameraEffect(liveCameraRepositoryImpl: LiveCameraRepositoryImpl): LiveCameraRepository

}