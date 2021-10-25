package za.co.woolworths.financial.services.android.ui.vto.data.prefstore

import kotlinx.coroutines.flow.Flow

interface PrefsStore {

    fun isLightingTipsGallery(): Flow<Boolean>
    fun isLightingTipsTakePhoto(): Flow<Boolean>
    fun isLightingTipsFiles(): Flow<Boolean>
    fun isLightingTipsLiveCamera(): Flow<Boolean>
    fun isTryItOnFirstTime(): Flow<Boolean>

    suspend fun disableLightingTipsGallery(isLighting: Boolean)

    suspend fun disableLightingFiles(isLighting: Boolean)

    suspend fun disableLightingTipsTakePhoto(isLighting: Boolean)

    suspend fun disableLightingTipsLiveCamera(isLighting: Boolean)

    suspend fun disableTryItOnMode(lighting: Boolean)
}