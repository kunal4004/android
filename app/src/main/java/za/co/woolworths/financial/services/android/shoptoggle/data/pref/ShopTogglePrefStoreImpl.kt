package za.co.woolworths.financial.services.android.shoptoggle.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.datastorepref.getPrefData
import za.co.woolworths.financial.services.android.util.datastorepref.setValue
import javax.inject.Inject


private const val PREF_NAME = "shop-toggle"
private val Context._dataStore: DataStore<Preferences> by preferencesDataStore(name = PREF_NAME)

class ShopTogglePrefStoreImpl @Inject constructor(
    @ApplicationContext context: Context,
) : ShopTogglePrefStore {
    private val dataStore: DataStore<Preferences> = context._dataStore


    override fun isShopToggleScreenFirstTime(): Flow<Boolean> =
        dataStore.getPrefData(SHOP_TOGGLE_SCREEN, true).catch { e ->
            FirebaseManager.logException(e)
        }

    override suspend fun disableShopToggleScreenFirstTime(isShopToggleScreen: Boolean) {
        dataStore.setValue(SHOP_TOGGLE_SCREEN, isShopToggleScreen)
    }

    companion object {
        private val SHOP_TOGGLE_SCREEN = booleanPreferencesKey("toggle")
    }

}