package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.local.storecard

import okhttp3.ResponseBody

//TODO : once we add room db this should be @Dao
interface StoreCardDao {
    suspend fun updateOrSaveData(responseBody: ResponseBody) {
    }
}