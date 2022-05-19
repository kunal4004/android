package za.co.woolworths.financial.services.android.util.wenum

import za.co.woolworths.financial.services.android.checkout.service.network.ConfirmDeliveryAddressResponse
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

open class ConfirmLocation {
    fun postConfirmLocation(
        confirmLocationRequest: ConfirmLocationRequest
    ) {
        val postConfirmLocation = GeoLocationApiHelper().initConfirmLocation(confirmLocationRequest)
        postConfirmLocation.enqueue(CompletionHandler(object :
            IResponseListener<ConfirmDeliveryAddressResponse> {
            override fun onSuccess(confirmLocationResponse: ConfirmDeliveryAddressResponse?) {
                confirmLocationResponse?.orderSummary?.fulfillmentDetails?.deliveryType?.let {
                    if (SessionUtilities.getInstance().isUserAuthenticated) {
                        Utils.savePreferredDeliveryLocation(
                            ShoppingDeliveryLocation(
                                confirmLocationResponse?.orderSummary?.fulfillmentDetails
                            )
                        )
                        if (KotlinUtils.getAnonymousUserLocationDetails() != null)
                            KotlinUtils.clearAnonymousUserLocationDetails()
                    } else {
                        KotlinUtils.saveAnonymousUserLocationDetails(
                            ShoppingDeliveryLocation(
                                confirmLocationResponse?.orderSummary?.fulfillmentDetails
                            )
                        )
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
            }
        }, ConfirmDeliveryAddressResponse::class.java))
    }

    fun postRequest(shoppingDeliveryLocation: ShoppingDeliveryLocation) {
        shoppingDeliveryLocation.fulfillmentDetails?.let {
            postConfirmLocation(
                ConfirmLocationRequest(
                    it.deliveryType ?: "",
                    ConfirmLocationAddress(it.address?.address1 ?: ""),
                    it.storeId ?: ""
                )
            )
        }
    }
}