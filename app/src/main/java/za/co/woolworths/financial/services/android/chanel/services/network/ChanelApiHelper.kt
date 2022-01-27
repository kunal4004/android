package za.co.woolworths.financial.services.android.chanel.services.network

import retrofit2.http.Query
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig

class ChanelApiHelper : RetrofitConfig() {

    suspend fun getBanners(
        searchTerm: String,
        searchType: String,
        resposneType: String,
        pageOffset: Int = 0,
        pageSize: Int = 60,
        filterContent: Boolean
    ) = mApiInterface.getChanelResponse(
        searchTerm,
        searchType,
        resposneType,
        pageOffset,
        pageSize,
        filterContent
    )
}