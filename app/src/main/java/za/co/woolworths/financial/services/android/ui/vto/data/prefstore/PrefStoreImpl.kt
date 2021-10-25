package za.co.woolworths.financial.services.android.ui.vto.data.prefstore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

    private val Context._dataStore: DataStore<Preferences> by preferencesDataStore(name = PREF_NAME)
    private val dataStore: DataStore<Preferences> = context._dataStore

    override fun isLightingTipsGallery() =
        dataStore.getPrefData(LIGHTING_TIPS_GALLERY, true)
            .catch {
                // handle error
            }

    override fun isLightingTipsFiles() =
        dataStore.getPrefData(LIGHTING_TIPS_FILES, true)
            .catch {
                // handle error
            }

    override fun isLightingTipsTakePhoto() =
        dataStore.getPrefData(LIGHTING_TIPS_TAKE_PHOTO, true)
            .catch {
                // handle error
            }

    override fun isLightingTipsLiveCamera() =
        dataStore.getPrefData(LIGHTING_TIPS_CAMERA, true)
            .catch {
                // handle error
            }

    override fun isTryItOnFirstTime() =
        dataStore.getPrefData(TRY_IT_ON, true)
            .catch {
                // handle error
            }

    override suspend fun disableLightingTipsGallery(lighting: Boolean) =
        dataStore.setValue(LIGHTING_TIPS_GALLERY, lighting)

    override suspend fun disableLightingFiles(isLighting: Boolean) =
        dataStore.setValue(LIGHTING_TIPS_FILES, isLighting)


    override suspend fun disableLightingTipsTakePhoto(isLighting: Boolean) =
        dataStore.setValue(LIGHTING_TIPS_TAKE_PHOTO, isLighting)

    override suspend fun disableLightingTipsLiveCamera(isLighting: Boolean) =
        dataStore.setValue(LIGHTING_TIPS_CAMERA, isLighting)

    override suspend fun disableTryItOnMode(tryItOn: Boolean) =
        dataStore.setValue(TRY_IT_ON, tryItOn)


    companion object {
        private val TRY_IT_ON = booleanPreferencesKey("try_it_on")
        private val LIGHTING_TIPS_GALLERY = booleanPreferencesKey("gallery")
        private val LIGHTING_TIPS_CAMERA = booleanPreferencesKey("camera")
        private val LIGHTING_TIPS_TAKE_PHOTO = booleanPreferencesKey("take_photo")
        private val LIGHTING_TIPS_FILES = booleanPreferencesKey("files")
    }


}