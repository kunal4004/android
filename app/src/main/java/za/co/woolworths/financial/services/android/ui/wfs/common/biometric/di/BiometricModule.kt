package za.co.woolworths.financial.services.android.ui.wfs.common.biometric.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.WfsBiometricManager
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.WfsBiometricManagerImpl


@Module
@InstallIn(ActivityComponent::class)
abstract class BiometricModule {

    @Binds
    abstract fun provideBiometricImpl(biometric : WfsBiometricManagerImpl): WfsBiometricManager

}