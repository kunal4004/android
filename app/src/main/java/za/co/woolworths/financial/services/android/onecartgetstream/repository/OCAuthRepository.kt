package za.co.woolworths.financial.services.android.onecartgetstream.repository

import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.onecartgetstream.network.OCAuthenticationResponse

interface OCAuthRepository {
    suspend fun getOCAuthToken(): Resource<OCAuthenticationResponse>
}