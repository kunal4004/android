package za.co.woolworths.financial.services.android.onecartgetstream.di

import dagger.Module
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.onecartgetstream.repository.OCAuthRepository
import za.co.woolworths.financial.services.android.onecartgetstream.repository.OCAuthRepositoryImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class OCAuthModule {

    @Binds
    abstract fun bindOCAuthRepository(ocAuthRepositoryImpl: OCAuthRepositoryImpl): OCAuthRepository
}