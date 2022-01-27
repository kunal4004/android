package za.co.woolworths.financial.services.android.chanel.services.repository

import za.co.woolworths.financial.services.android.chanel.services.network.ChanelApiHelper

class ChanelRepository(val chanelApiHelper: ChanelApiHelper) {

    suspend fun getChanelBannerData() = chanelApiHelper.getBanners()
}