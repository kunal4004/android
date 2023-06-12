package za.co.woolworths.financial.services.android.domain.usecase

import com.awfs.coordination.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.network.Resource
import java.io.IOException
import javax.inject.Inject

class AddToListUC @Inject constructor(
    private val myListRepository: MyListRepository
) {

    operator fun invoke(
        listId: String,
        products: List<AddToListRequest>
    ): Flow<Resource<ShoppingListItemsResponse>> = flow {
        try {
            emit(Resource.loading(null))
            val result = myListRepository.addProductsToListById(listId, products)
            if (result.isSuccessful && result.body() != null) {
                emit(Resource.success(result.body()))
            } else
                emit(Resource.error(R.string.error_occured, result.body()))
        } catch (e: HttpException) {
            emit(Resource.error(R.string.error_occured, null))
        } catch (e: IOException) {
            emit(Resource.error(R.string.error_internet_connection, null))
        }
    }
}