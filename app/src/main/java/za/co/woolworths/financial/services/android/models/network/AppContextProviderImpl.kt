package za.co.woolworths.financial.services.android.models.network

import android.content.Context
import za.co.woolworths.financial.services.android.models.WoolworthsApplication

class AppContextProviderImpl: AppContextProviderInterface {
    override fun appContext(): Context = WoolworthsApplication.getAppContext()
}