package za.co.woolworths.financial.services.android.ui.fragments.account.main.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.local.AccountDataClass
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton // without this count will increase every time you inject TestingClass
    fun provideTestClass(): AccountDataClass {
        return AccountDataClass()
    }
}