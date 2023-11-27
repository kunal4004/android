package za.co.woolworths.financial.services.android.shoptoggle.domain.usecase

import android.content.Context
import com.awfs.coordination.R
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.checkout.service.network.ConfirmDeliveryAddressResponse
import za.co.woolworths.financial.services.android.common.ResourcesProvider
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.models.dto.cart.FulfillmentDetails
import za.co.woolworths.financial.services.android.shoptoggle.data.dto.ShopToggleData
import za.co.woolworths.financial.services.android.shoptoggle.data.mapper.toDomain
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.domain.repository.ShopToggleRepository
import za.co.woolworths.financial.services.android.ui.extension.isConnectedToNetwork
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.getDeliveryType
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.io.IOException
import javax.inject.Inject

class ShopToggleUseCase @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val shopToggleRepository: ShopToggleRepository,
    private val geoLocationApiHelper: GeoLocationApiHelper
) {
    private var validateLocationResponse: ValidateLocationResponse? = null

    companion object {
        const val STANDARD_DELIVERY_ID = 1
        const val DASH_DELIVERY_ID = 2
        const val CNC_DELIVERY_ID = 3
    }

    fun validateLocationResponse(): ValidateLocationResponse? {
        return validateLocationResponse
    }

    fun getValidateLocationDetails(placeId: String?) = flow {
        if (placeId.isNullOrEmpty()) {
            emit(Resource.Success(data = getFailureData()))
        } else {
            try {
                emit(Resource.Loading())
                validateLocationResponse = shopToggleRepository.getValidateLocation(placeId)
                when (validateLocationResponse?.httpCode) {
                    AppConstant.HTTP_OK -> {
                        val validatePlace = validateLocationResponse?.validatePlace
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
        dashModel.deliveryCost = "R $deliveryPrice.00"
        dashModel.foodQuantity = foodQuantity ?: 0
        //Prepare CNC Data
        val cncModel = getCncData()
        val store = GeoUtils.getStoreDetails(
            getDeliveryType()?.storeId,
            stores
        ) ?: stores?.get(0)
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
        dashModel.deliveryCost = "R 35.00"
        dashModel.foodQuantity = foodQuantity
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
        id = STANDARD_DELIVERY_ID,
        title = resourcesProvider.getString(R.string.use_standard_delivery),
        subTitle = resourcesProvider.getString(R.string.standard_shop_fashion),
        icon = R.drawable.ic_toggle_delivery_truck,
        deliveryTypeLabel = resourcesProvider.getString(R.string.earliest_standard_delivery_dates),
        deliveryCost = resourcesProvider.getString(R.string.delivery_cost),
        deliverySlotFood = "",
        deliverySlotFbh = "",
        learnMore = resourcesProvider.getString(R.string.determined_at_checkout),
        deliveryButtonText = resourcesProvider.getString(R.string.set_to_standard_delivery),
        deliveryButtonTextContinue = resourcesProvider.getString(R.string.continue_with_standard_delivery),
        quantity = 0,
        isDashDelivery = false,
        deliveryType = Delivery.STANDARD.type
    ).toDomain()

    private fun getDashData() = ShopToggleData(
        id = DASH_DELIVERY_ID,
        title = resourcesProvider.getString(R.string.use_dash_delivery),
        subTitle = resourcesProvider.getString(R.string.get_food_today),
        icon = R.drawable.ic_toggle_dash_scooter,
        deliveryTypeLabel = resourcesProvider.getString(R.string.next_dash_delivery_timeslot),
        deliveryCost = resourcesProvider.getString(R.string.delivery_cost),
        deliverySlotFood = "",
        deliverySlotFbh = "",
        learnMore = resourcesProvider.getString(R.string.determined_at_checkout),
        deliveryButtonText = resourcesProvider.getString(R.string.set_to_dash_delivery),
        deliveryButtonTextContinue = resourcesProvider.getString(R.string.continue_with_dash_delivery),
        quantity = 0,
        deliveryType = Delivery.DASH.type,
        isDashDelivery = true
    ).toDomain()

    private fun getCncData() = ShopToggleData(
        id = CNC_DELIVERY_ID,
        title = resourcesProvider.getString(R.string.use_click_collect),
        subTitle = resourcesProvider.getString(R.string.collect_fashion_food),
        icon = R.drawable.ic_toggle_collection_bag,
        deliveryTypeLabel = resourcesProvider.getString(R.string.earliest_click_and_collect),
        deliveryCost = resourcesProvider.getString(R.string.collection_cost),
        deliverySlotFood = "",
        deliverySlotFbh = "",
        learnMore = resourcesProvider.getString(R.string.determined_at_checkout),
        deliveryButtonText = resourcesProvider.getString(R.string.set_to_click_and_collect),
        deliveryButtonTextContinue = resourcesProvider.getString(R.string.continue_with_cnc_delivery),
        quantity = 0,
        deliveryType = Delivery.CNC.type,
        isDashDelivery = false
    ).toDomain()

    private suspend fun postConfirmAddress(confirmLocationRequest: ConfirmLocationRequest) =
        geoLocationApiHelper.postConfirmLocation(confirmLocationRequest)

    fun isConnectedToInternet(context: Context) =
        geoLocationApiHelper.isConnectedToInternet(context)

    private fun getUnsellableItems(deliveryType: Delivery): List<UnSellableCommerceItem>? {
        var unSellableCommerceItems: List<UnSellableCommerceItem>? = emptyList()
        when (deliveryType.name) {
            Delivery.STANDARD.name -> {
                unSellableCommerceItems =
                    validateLocationResponse?.validatePlace?.unSellableCommerceItems
            }

            Delivery.CNC.name -> {
            //TODO, fix this later on

                val mStoreId= getDeliveryType()?.storeId
               validateLocationResponse?.validatePlace?.stores?.forEach {
                    if (it.storeId.equals(mStoreId)) {
                        unSellableCommerceItems = it.unSellableCommerceItems
                    }
                }
             //   currentDeliveryType = Delivery.CNC
            }

            Delivery.DASH.name -> {
                unSellableCommerceItems =
                    validateLocationResponse?.validatePlace?.onDemand?.unSellableCommerceItems
            }
        }
        return unSellableCommerceItems
    }

    fun sendConfirmLocation(deliveryType: Delivery) =
        flow<Resource<List<UnSellableCommerceItem>>> {
            if (isConnectedToNetwork() != true) {
                emit(Resource.Error(message = resourcesProvider.getString(R.string.no_internet_title)))
                return@flow
            }
            val unsellableItems = getUnsellableItems(deliveryType)
            if (!unsellableItems.isNullOrEmpty() && SessionUtilities.getInstance().isUserAuthenticated) {
                emit(Resource.Success(data = unsellableItems))
            } else {
                emit(Resource.Loading())
                try {
                    val confirmLocationRequest = getConfirmLocationRequest(deliveryType)
                    if (confirmLocationRequest.address.placeId.isNullOrEmpty()) {
                        emit(Resource.Error(message = resourcesProvider.getString(R.string.no_internet_title)))
                        return@flow
                    }
                    val confirmLocationResponse = postConfirmAddress(confirmLocationRequest)

                    when (confirmLocationResponse.httpCode) {
                        AppConstant.HTTP_OK, AppConstant.HTTP_OK_201 -> {
                            saveResponse(confirmLocationResponse, confirmLocationRequest)
                            emit(Resource.Success(data = null))
                        }

                        else -> {
                            emit(Resource.Error(message = resourcesProvider.getString(R.string.no_internet_title)))
                        }
                    }
                } catch (e: Exception) {
                    FirebaseManager.logException(e)
                    emit(Resource.Error(message = resourcesProvider.getString(R.string.no_internet_title)))
                }
            }
        }

    private fun getConfirmLocationRequest(deliveryType: Delivery?): ConfirmLocationRequest {
        val deliverType = getDeliveryType()
        val placeId = deliverType?.address?.placeId
        var storeId = deliverType?.storeId
        val confirmLocationAddress = ConfirmLocationAddress(placeId)
        return when (deliveryType) {
            Delivery.STANDARD -> {
                ConfirmLocationRequest(BundleKeysConstants.STANDARD, confirmLocationAddress, "")
            }

            Delivery.CNC -> {
                ConfirmLocationRequest(BundleKeysConstants.CNC, confirmLocationAddress, storeId)
            }

            Delivery.DASH -> {
                storeId = validateLocationResponse?.validatePlace?.onDemand?.storeId
                ConfirmLocationRequest(BundleKeysConstants.DASH, confirmLocationAddress, storeId)
            }

            else -> {
                ConfirmLocationRequest(BundleKeysConstants.STANDARD, confirmLocationAddress, "")
            }
        }
    }

    private fun saveResponse(
        confirmLocationResponse: ConfirmDeliveryAddressResponse,
        confirmLocationRequest: ConfirmLocationRequest
    ) {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            Utils.savePreferredDeliveryLocation(
                ShoppingDeliveryLocation(
                    confirmLocationResponse.orderSummary?.fulfillmentDetails
                )
            )
            if (KotlinUtils.getAnonymousUserLocationDetails() != null) KotlinUtils.clearAnonymousUserLocationDetails()
        } else {
            KotlinUtils.saveAnonymousUserLocationDetails(
                ShoppingDeliveryLocation(
                    confirmLocationResponse.orderSummary?.fulfillmentDetails
                )
            )
        }
        val savedPlaceId = KotlinUtils.getDeliveryType()?.address?.placeId
        KotlinUtils.apply {
            this.placeId = confirmLocationRequest.address.placeId
            isLocationPlaceIdSame = confirmLocationRequest.address.placeId?.equals(
                savedPlaceId
            )
        }
    }
}

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}