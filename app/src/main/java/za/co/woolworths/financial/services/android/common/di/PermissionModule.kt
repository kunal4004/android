package za.co.woolworths.financial.services.android.common.di

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
class PermissionModule {

    @Provides
    fun provideActivityResultRegistry(@ActivityContext activity: Context) =
        (activity as? AppCompatActivity)?.activityResultRegistry
            ?: throw IllegalArgumentException("must use AppCompatActivity")
}