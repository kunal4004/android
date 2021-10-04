package za.co.woolworths.financial.services.android.ui.vto.prefstore

import kotlinx.coroutines.flow.Flow

interface PrefsStore {

    fun isLightingTipsFirstTime(): Flow<Boolean>

    suspend fun disableLightingTips(lighting: Boolean)

    fun isTryItOnFirstTime(): Flow<Boolean>

    suspend fun disableTryItOnMode(lighting: Boolean)


}