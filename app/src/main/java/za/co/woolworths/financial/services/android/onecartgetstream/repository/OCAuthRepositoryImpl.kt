package za.co.woolworths.financial.services.android.onecartgetstream.repository

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.onecartgetstream.model.OCAuthenticationResponse
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException
import javax.inject.Inject

class OCAuthRepositoryImpl @Inject constructor() : OCAuthRepository {

    override suspend fun getOCAuthToken(): Resource<OCAuthenticationResponse> {

        return try {
            val response = OneAppService.getOCAuthData()
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.success(it)
                } ?: Resource.error(R.string.error_unknown, null)
            } else {
                Resource.error(R.string.error_unknown, null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }

    }

}