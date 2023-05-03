package za.co.woolworths.financial.services.android.models.network

import android.content.Context
import org.mockito.Mockito

class AppContextProviderStub: AppContextProviderInterface {
    override fun appContext(): Context = Mockito.mock(Context::class.java)
}