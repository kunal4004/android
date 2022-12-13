package za.co.woolworths.financial.services.android.util

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object HuaweiAppModule {

    @Provides
    fun provideHuaweiQualityCrashInstance() = HuaweiQualityCrashInstance()

}