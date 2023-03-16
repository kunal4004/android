package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ShoppingListDetailsRepositoryModule {

    @ViewModelScoped
    @Provides
    fun provideMainShoppingListDetailsRepository() = MainShoppingListDetailRepository() as ShoppingListDetailRepository
}