package za.co.woolworths.financial.services.android.ui.vto.di


import dagger.Binds
import dagger.Module

import dagger.hilt.InstallIn

import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.ui.vto.prefstore.PrefStoreImpl
import za.co.woolworths.financial.services.android.ui.vto.prefstore.PrefsStore

@Module
@InstallIn(ViewModelComponent::class)
abstract class VtoStoreModule {

    @Binds
    abstract fun bindPrefsStore(prefsStoreImpl: PrefStoreImpl): PrefsStore

}


