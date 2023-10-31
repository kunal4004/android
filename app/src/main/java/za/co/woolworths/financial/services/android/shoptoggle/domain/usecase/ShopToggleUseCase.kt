package za.co.woolworths.financial.services.android.shoptoggle.domain.usecase

import com.awfs.coordination.R
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.common.ResourcesProvider
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.models.dto.cart.FulfillmentDetails
import za.co.woolworths.financial.services.android.shoptoggle.data.dto.ShopToggleData
import za.co.woolworths.financial.services.android.shoptoggle.data.mapper.toDomain
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.domain.repository.ShopToggleRepository
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.io.IOException
import javax.inject.Inject

class ShopToggleUseCase @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val shopToggleRepository: ShopToggleRepository

) {

    companion object {
        const val STANDARD_DELIVERY_ID = 1
        const val DASH_DELIVERY_ID = 2
        const val CNC_DELIVERY_ID = 3
    }

//    operator fun invoke(): List<ToggleModel> {
//        return shopToggleRepository.getShopToggleList().map { it.toDomain() }
//    }

    fun getValidateLocationDetails1(placeId: String?) = flow {
        if (placeId.isNullOrEmpty()) {
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
                        fulfillmentDeliveryLocation?.fulfillmentDetails?.address?.nickname =
                            nickname
                        Utils.savePreferredDeliveryLocation(fulfillmentDeliveryLocation)

                        val toggleModels =
                            validatePlace?.toToggleFulfilmentList() ?: getFailureData()
                        emit(Resource.Success(data = toggleModels))
                    }

                    else -> {
                        emit(Resource.Error(message = resourcesProvider.getString(R.string.common_error_unfortunately_something_went_wrong)))
                    }
                }
            } catch (e: HttpException) {
                FirebaseManager.logException(e)
                emit(Resource.Error(message = resourcesProvider.getString(R.string.common_error_unfortunately_something_went_wrong)))
            } catch (e: IOException) {
                FirebaseManager.logException(e)
                emit(Resource.Error(message = resourcesProvider.getString(R.string.common_error_unfortunately_something_went_wrong)))
            } catch (e: Exception) {
                FirebaseManager.logException(e)
                emit(Resource.Error(message = resourcesProvider.getString(R.string.common_error_unfortunately_something_went_wrong)))
            }
        }
    }

    private fun ValidatePlace.toToggleFulfilmentList(): List<ToggleModel> {
        //Prepare STANDARD Data
        val standardModel = getStandardData()
        standardModel.deliverySlotFood = firstAvailableFoodDeliveryDate ?: ""
        standardModel.deliverySlotFbh = firstAvailableOtherDeliveryDate ?: ""
        //Prepare DASH Data
        val dashModel = getDashData()
        val timeSlots = onDemand?.deliveryTimeSlots
        if (timeSlots?.isEmpty() == true && onDemand?.deliverable == true) {
            dashModel.deliverySlotFood =
                resourcesProvider.getString(R.string.no_timeslots_available_title)
        } else {
            dashModel.deliverySlotFood = onDemand?.firstAvailableFoodDeliveryTime ?: ""
        }
        val foodQuantity = onDemand?.quantityLimit?.foodMaximumQuantity
        val deliveryPrice = onDemand?.firstAvailableFoodDeliveryCost
        dashModel.deliveryCost = deliveryPrice.toString()
        dashModel.foodQuantity = foodQuantity ?: 0
        //Prepare CNC Data
        val cncModel = getCncData()
        val store = stores?.get(0)
        val foodDeliveryDate = store?.firstAvailableFoodDeliveryDate ?: resourcesProvider.getString(
            R.string.no_timeslots_available_title
        )
        val fbhDeliveryDate = store?.firstAvailableOtherDeliveryDate ?: resourcesProvider.getString(
            R.string.no_timeslots_available_title
        )
        cncModel.deliverySlotFood = foodDeliveryDate
        cncModel.deliverySlotFbh = fbhDeliveryDate
        return prepareDeliveryList(standardModel, dashModel, cncModel)
    }

    private fun prepareDeliveryList(
        standardModel: ToggleModel, dashModel: ToggleModel, cncModel: ToggleModel
    ): List<ToggleModel> {
        val list = mutableListOf(standardModel, dashModel, cncModel)

        val fulfillmentDetails: FulfillmentDetails? = KotlinUtils.getDeliveryType()
        fulfillmentDetails?.apply {
            when (Delivery.getType(deliveryType)) {
                Delivery.CNC -> {
                    list.remove(cncModel)
                    list.add(0, cncModel)
                }

                Delivery.DASH -> {
                    list.remove(dashModel)
                    list.add(0, dashModel)
                }

                else -> {
                    list.remove(standardModel)
                    list.add(0, standardModel)
                }
            }
        }
        return list
    }

    fun getFailureData(): List<ToggleModel> {
        ///////////////STANDARD///////////////////////
        val standardModel = getStandardData()
        standardModel.dataFailure = true
        ///////////////DASH///////////////////////
        val dashModel = getDashData()
        dashModel.dataFailure = true
        val foodQuantity = 30
        dashModel.deliveryCost = "R 35"
        ///////////////CNC///////////////////////
        val cncModel = getCncData()
        cncModel.dataFailure = true
        return listOf(standardModel, dashModel, cncModel)
    }

    fun getSelectedDeliveryId(): Int? {
        val fulfillmentDetails: FulfillmentDetails? = KotlinUtils.getDeliveryType()
        if (fulfillmentDetails == null) {
            return null
        } else {
            return when (Delivery.getType(fulfillmentDetails.deliveryType)) {
                Delivery.STANDARD -> {
                    STANDARD_DELIVERY_ID
                }

                Delivery.DASH -> {
                    DASH_DELIVERY_ID
                }

                Delivery.CNC -> {
                    CNC_DELIVERY_ID
                }

                else -> {
                    null
                }
            }
        }
    }

    private fun getStandardData() = ShopToggleData(
        id = 1,
        title = resourcesProvider.getString(R.string.use_standard_delivery),
        subTitle = resourcesProvider.getString(R.string.standard_shop_fashion),
        icon = R.drawable.ic_toggle_delivery_truck,
        deliveryTypeLabel = resourcesProvider.getString(R.string.earliest_standard_delivery_dates),
        deliveryCost = resourcesProvider.getString(R.string.delivery_cost),
        deliverySlotFood = "",
        deliverySlotFbh = "",
        learnMore = resourcesProvider.getString(R.string.determined_at_checkout),
        deliveryButtonText = resourcesProvider.getString(R.string.set_to_standard_delivery),
        quantity = 0,
        isDashDelivery = false,
        deliveryType = Delivery.STANDARD.type
    ).toDomain()

    private fun getDashData() = ShopToggleData(
        id = 2,
        title = resourcesProvider.getString(R.string.use_dash_delivery),
        subTitle = resourcesProvider.getString(R.string.get_food_today),
        icon = R.drawable.ic_toggle_dash_scooter,
        deliveryTypeLabel = resourcesProvider.getString(R.string.next_dash_delivery_timeslot),
        deliveryCost = resourcesProvider.getString(R.string.delivery_cost),
        deliverySlotFood = "",
        deliverySlotFbh = "",
        learnMore = resourcesProvider.getString(R.string.determined_at_checkout),
        deliveryButtonText = resourcesProvider.getString(R.string.set_to_dash_delivery),
        quantity = 0,
        deliveryType = Delivery.DASH.type,
        isDashDelivery = true
    ).toDomain()

    private fun getCncData() = ShopToggleData(
        id = 3,
        title = resourcesProvider.getString(R.string.use_click_collect),
        subTitle = resourcesProvider.getString(R.string.collect_fashion_food),
        icon = R.drawable.ic_toggle_collection_bag,
        deliveryTypeLabel = resourcesProvider.getString(R.string.earliest_click_and_collect),
        deliveryCost = resourcesProvider.getString(R.string.delivery_cost),
        deliverySlotFood = "",
        deliverySlotFbh = "",
        learnMore = resourcesProvider.getString(R.string.determined_at_checkout),
        deliveryButtonText = resourcesProvider.getString(R.string.set_to_click_and_collect),
        quantity = 0,
        deliveryType = Delivery.CNC.type,
        isDashDelivery = false
    ).toDomain()
}

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}



