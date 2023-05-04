package za.co.woolworths.financial.services.android.checkout.repository

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException
import javax.inject.Inject

class CheckoutLiquorRepositoryImpl @Inject constructor() : CheckoutLiquorRepository {
    override suspend fun getShoppingCartData(): Resource<ShoppingCartResponse> {
        return try {
            val response = OneAppService().getShoppingCartV2()
            if (response.isSuccessful) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 ->
                            Resource.success(it)
                        AppConstant.HTTP_NOT_FOUND_404 ->
                                Resource.error(R.string.request_timeout_error, null)
                        else ->
                            Resource.error(R.string.error_unknown, null)
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