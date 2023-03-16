package za.co.woolworths.financial.services.android.enhancedSubstitution.repository

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.ProductSubstitution
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException

class ProductSubstitutionRepository {

    suspend fun getProductSubstitution(productId: String?): Resource<ProductSubstitution> {
        return try {
            val response = OneAppService.getProductSubstitution(productId)
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(it)
                        else ->
                            Resource.error(R.string.error_unknown, it)
                    }
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