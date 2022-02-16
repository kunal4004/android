package za.co.woolworths.financial.services.android.ui.vto.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.ui.vto.data.prefstore.PrefStoreImpl
import za.co.woolworths.financial.services.android.ui.vto.data.prefstore.PrefsStore

@Module
@InstallIn(ViewModelComponent::class)
abstract class VtoStoreModule {

    @Binds
    abstract fun bindPrefsStore(prefsStoreImpl: PrefStoreImpl): PrefsStore

}


