package za.co.woolworths.financial.services.android.ui.vto.data.prefstore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.datastorepref.getPrefData
import za.co.woolworths.financial.services.android.util.datastorepref.setValue
import javax.inject.Inject

private const val PREF_NAME = "wool-retail"
private val Context._dataStore: DataStore<Preferences> by preferencesDataStore(name = PREF_NAME)

class PrefStoreImpl
@Inject constructor(
    @ApplicationContext context: Context

) : PrefsStore {

    private val dataStore: DataStore<Preferences> = context._dataStore

    override fun isLightingTipsFirstTime() =
        dataStore.getPrefData(LIGHTING_TIPS, true)
            .catch { e ->
                FirebaseManager.logException(e)
            }

    override fun isTryItOnFirstTime() =
        dataStore.getPrefData(TRY_IT_ON, true)
            .catch { e ->
                FirebaseManager.logException(e)
            }

    override suspend fun disableLightingTips(isLighting: Boolean) =
        dataStore.setValue(LIGHTING_TIPS, isLighting)

    override suspend fun disableTryItOnMode(isTryItOn: Boolean) =
        dataStore.setValue(TRY_IT_ON, isTryItOn)


    companion object {
        private val TRY_IT_ON = booleanPreferencesKey("try_it_on")
        private val LIGHTING_TIPS = booleanPreferencesKey("lighting_tips")

    }


}