package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.local

import com.squareup.okhttp.ResponseBody

//TODO : once we add room db this should be @Dao
interface StoreCardDao {
    suspend fun updateOrSaveData(responseBody: ResponseBody) {
    }
}