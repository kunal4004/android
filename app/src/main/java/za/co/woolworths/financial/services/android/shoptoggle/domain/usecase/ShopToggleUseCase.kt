package za.co.woolworths.financial.services.android.shoptoggle.domain.usecase

import com.awfs.coordination.R
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.common.ResourcesProvider
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.shoptoggle.data.mapper.toDomain
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.domain.repository.ShopToggleRepository
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException
import javax.inject.Inject

class ShopToggleUseCase @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val shopToggleRepository: ShopToggleRepository

) {
    operator fun invoke(): List<ToggleModel> {
        return shopToggleRepository.getShopToggleList().map { it.toDomain() }
    }

    fun getValidateLocationDetails1(placeId: String?)  = flow {
        if (placeId.isNullOrEmpty()) {
            // TODO, handle this case
            emit(Resource.Success(data = getFailureData()))
        } else {
            try {
                emit(Resource.Loading())
                val validateResponse = shopToggleRepository.getValidateLocation(placeId)
                when (validateResponse.httpCode) {
                    AppConstant.HTTP_OK -> {
                        val validatePlace = validateResponse.validatePlace
                        KotlinUtils.placeId = validatePlace?.placeDetails?.placeId
                        val nickname = validatePlace?.placeDetails?.nickname
                        val fulfillmentDeliveryLocation = Utils.getPreferredDeliveryLocation()
                        fulfillmentDeliveryLocation?.fulfillmentDetails?.address?.nickname = nickname
                        Utils.savePreferredDeliveryLocation(fulfillmentDeliveryLocation)

                        val toggleModels = validatePlace?.toToggleFulfilmentList() ?: getFailureData()
                        emit(Resource.Success(data = toggleModels))
                    }
                    else -> {
                        emit(Resource.Error(message = "Something went wrong, please try again later"))
                    }
                }
            } catch (e: HttpException) {
                FirebaseManager.logException(e)
                //emit(Resource.Success(data = getFailureData()))
                emit(Resource.Error(message = "Something went wrong, please try again later"))
            } catch (e: IOException) {
                FirebaseManager.logException(e)
                //emit(Resource.Success(data = getFailureData()))
                emit(Resource.Error(message = "Network error, please check your internet connection"))
            } catch (e: Exception) {
                FirebaseManager.logException(e)
                //emit(Resource.Success(data = getFailureData()))
                emit(Resource.Error(message = "Something went wrong, please try again later"))
            }
        }
    }

    private fun ValidatePlace.toToggleFulfilmentList(): List<ToggleModel> {
        ///////////////STANDARD///////////////////////
        val standardModel = this@ShopToggleUseCase()[0]
        standardModel.deliveryTime = firstAvailableFoodDeliveryDate ?: ""
        standardModel.deliveryProduct = firstAvailableOtherDeliveryDate ?: ""
        ///////////////DASH///////////////////////
        val dashModel = this@ShopToggleUseCase()[1]
        val timeSlots = onDemand?.deliveryTimeSlots
        if (timeSlots?.isEmpty() == true && onDemand?.deliverable == true) {
            dashModel.deliveryTime =
                resourcesProvider.getString(R.string.no_timeslots_available_title)
        } else {
            dashModel.deliveryTime = onDemand?.firstAvailableFoodDeliveryTime ?: ""
        }
        val foodQuantity = onDemand?.quantityLimit?.foodMaximumQuantity
        val deliveryPrice = onDemand?.firstAvailableFoodDeliveryCost
        dashModel.deliveryProduct = "Food only *Limited shop of $foodQuantity food items"
        dashModel.learnMore = deliveryPrice.toString()
        ///////////////CNC///////////////////////
        val cncModel = this@ShopToggleUseCase()[2]
        val store = stores?.get(0)
        val foodDeliveryDate =
            "Food: ${store?.firstAvailableFoodDeliveryDate ?: "No timeslots available"}"
        val fbhDeliveryDate =
            "Fashion, Beauty, Home: ${store?.firstAvailableOtherDeliveryDate ?: "No timeslots available"}"
        cncModel.deliveryTime = foodDeliveryDate
        cncModel.deliveryProduct = fbhDeliveryDate
        return listOf(standardModel, dashModel, cncModel)
    }

    fun getFailureData(): List<ToggleModel> {
        ///////////////STANDARD///////////////////////
        val standardModel = this@ShopToggleUseCase()[0]
        standardModel.dataFailure = true
        ///////////////DASH///////////////////////
        val dashModel = this@ShopToggleUseCase()[1]
        dashModel.dataFailure = true
        val foodQuantity = 30
        dashModel.deliveryProduct = "Food only *Limited shop of $foodQuantity food items"
        dashModel.learnMore = "R 35"
        ///////////////CNC///////////////////////
        val cncModel = this@ShopToggleUseCase()[2]
        cncModel.dataFailure = true
        return listOf(standardModel, dashModel, cncModel)
    }
}

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}



