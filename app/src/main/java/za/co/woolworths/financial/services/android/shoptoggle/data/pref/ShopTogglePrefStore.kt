package za.co.woolworths.financial.services.android.shoptoggle.data.pref

import kotlinx.coroutines.flow.Flow

interface ShopTogglePrefStore {

    fun isShopToggleScreenFirstTime(): Flow<Boolean>
    suspend fun disableShopToggleScreenFirstTime(isShopToggleScreen: Boolean)
}