package za.co.woolworths.financial.services.android.ui.vto.prefstore

import android.content.Context
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import za.co.woolworths.financial.services.android.util.datastorepref.getPrefData
import za.co.woolworths.financial.services.android.util.datastorepref.setValue
import javax.inject.Inject

private const val PREF_NAME = "wool-retail"

class PrefStoreImpl
@Inject constructor(
    @ApplicationContext context: Context
) : PrefsStore {


    private val dataStore = context.createDataStore(
        name = PREF_NAME
    )

    override fun isLightingTipsFirstTime() =
        dataStore.getPrefData(LIGHTING_TIPS, true)
            .catch {
                // handle error
            }

    override suspend fun disableLightingTips(lighting: Boolean) {
        dataStore.setValue(LIGHTING_TIPS, lighting)
    }

    override fun isTryItOnFirstTime() =
        dataStore.getPrefData(TRY_IT_ON, true)
            .catch {
                // handle error
            }


    override suspend fun disableTryItOnMode(tryItOn: Boolean) {
        dataStore.setValue(TRY_IT_ON, tryItOn)
    }

    companion object {
        private val TRY_IT_ON = preferencesKey<Boolean>("try_it_on")
        private val LIGHTING_TIPS = preferencesKey<Boolean>("lighting_tips")
    }


}