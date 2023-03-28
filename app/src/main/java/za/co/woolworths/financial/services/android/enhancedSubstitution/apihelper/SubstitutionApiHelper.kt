package za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper

import za.co.woolworths.financial.services.android.models.network.RetrofitConfig


class SubstitutionApiHelper : RetrofitConfig() {

    suspend fun getProductSubstitution(productId: String?) = mApiInterface.getSubstitution(
            getSessionToken(),
            getDeviceIdentityToken(),
            productId
    )
}