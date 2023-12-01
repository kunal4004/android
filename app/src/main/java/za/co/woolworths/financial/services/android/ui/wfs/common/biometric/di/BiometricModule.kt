package za.co.woolworths.financial.services.android.ui.wfs.common.biometric.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.WfsBiometricManager
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.WfsBiometricManagerImpl
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class BiometricModule {

    @Singleton
    @Binds
     abstract fun provideBiometricImpl(biometric : WfsBiometricManagerImpl): WfsBiometricManager

}