package za.co.woolworths.financial.services.android.util.wenum

import android.app.Activity
import android.content.Intent
import za.co.woolworths.financial.services.android.checkout.service.network.ConfirmDeliveryAddressResponse
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity.SSOActivityResult
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

open class ConfirmLocation {
    private fun postConfirmLocation(
        confirmLocationRequest: ConfirmLocationRequest, activity: Activity, intent: Intent,
    ) {
        if (confirmLocationRequest.address.placeId.isNullOrEmpty()) {
            return
        }
        val postConfirmLocation = GeoLocationApiHelper().initConfirmLocation(confirmLocationRequest)
        postConfirmLocation.enqueue(CompletionHandler(object :
            IResponseListener<ConfirmDeliveryAddressResponse> {
            override fun onSuccess(confirmLocationResponse: ConfirmDeliveryAddressResponse?) {
                confirmLocationResponse?.orderSummary?.fulfillmentDetails?.let {
                    if (SessionUtilities.getInstance().isUserAuthenticated) {
                        KotlinUtils.clearAnonymousUserLocationDetails()
                        Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(it))
                    } else {
                        KotlinUtils.saveAnonymousUserLocationDetails(ShoppingDeliveryLocation(it))
                    }
                }
                activity?.setResult(SSOActivityResult.SUCCESS.rawValue(), intent)
                if (activity is SSOActivity) {
                    activity.closeActivity()
                }
            }

            override fun onFailure(error: Throwable?) {
                if (activity is SSOActivity) {
                    activity.closeActivity()
                }
            }
        }, ConfirmDeliveryAddressResponse::class.java))
    }

    fun postRequest(
        shoppingDeliveryLocation: ShoppingDeliveryLocation,
        isUserBrowsing: Boolean,
        activity: Activity,
        intent: Intent,
    ) {
        if (isUserBrowsing) {
            postConfirmLocation(KotlinUtils.getConfirmLocationRequest(KotlinUtils.browsingDeliveryType),
                activity,
                intent)
        } else {
            shoppingDeliveryLocation.fulfillmentDetails?.let {
                postConfirmLocation(
                    ConfirmLocationRequest(
                        it.deliveryType ?: "",
                        ConfirmLocationAddress(it.address?.placeId ?: ""),
                        it.storeId ?: ""
                    ), activity, intent
                )
            }
        }
    }
}