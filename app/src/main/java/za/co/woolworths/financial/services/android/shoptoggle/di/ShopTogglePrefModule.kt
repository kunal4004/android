package za.co.woolworths.financial.services.android.shoptoggle.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import za.co.woolworths.financial.services.android.shoptoggle.data.pref.ShopTogglePrefStore
import za.co.woolworths.financial.services.android.shoptoggle.data.pref.ShopTogglePrefStoreImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class ShopTogglePrefModule {

    @Binds
    abstract fun bindPrefsStore(shopTogglePrefStoreImpl: ShopTogglePrefStoreImpl): ShopTogglePrefStore

}
