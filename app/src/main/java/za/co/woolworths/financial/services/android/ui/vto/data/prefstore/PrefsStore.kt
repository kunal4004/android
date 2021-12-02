package za.co.woolworths.financial.services.android.ui.vto.data.prefstore

import kotlinx.coroutines.flow.Flow

interface PrefsStore {

    fun isLightingTipsFirstTime(): Flow<Boolean>
    fun isTryItOnFirstTime(): Flow<Boolean>
    suspend fun disableLightingTips(isLighting: Boolean)
    suspend fun disableTryItOnMode(lighting: Boolean)
}