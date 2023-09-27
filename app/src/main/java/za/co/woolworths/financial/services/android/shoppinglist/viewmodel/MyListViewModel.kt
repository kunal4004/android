package za.co.woolworths.financial.services.android.shoppinglist.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.awfs.coordination.R
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.common.ResourcesProvider
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.shoppinglist.component.LocationDetailsState
import za.co.woolworths.financial.services.android.shoppinglist.component.MyLIstUIEvents
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 26/09/23.
 */

@HiltViewModel
class MyListViewModel @Inject constructor(
    private val resources: ResourcesProvider,
) : ViewModel() {

    var deliveryDetailsState = mutableStateOf(LocationDetailsState())

    fun onEvent(events: MyLIstUIEvents) {
        when (events) {
            is MyLIstUIEvents.SetDeliveryLocation -> setDeliveryDetails()
            else -> Unit
        }
    }

    private fun setDeliveryDetails() {
        Utils.getPreferredDeliveryLocation().fulfillmentDetails?.let {
            deliveryDetailsState.value = when (Delivery.getType(it?.deliveryType)) {
                Delivery.CNC -> {
                    deliveryDetailsState.value.copy(
                        icon = R.drawable.ic_collection_circle,
                        deliveryType = resources.getString(R.string.collecting_from),
                        deliveryLocation = it?.storeName ?: ""
                    )
                }

                Delivery.DASH -> {
                    var timeSlot: String? =
                        WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.firstAvailableFoodDeliveryTime
                    timeSlot =
                        if (timeSlot?.isNullOrEmpty() == true || WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.deliveryTimeSlots?.isNullOrEmpty() == true) {
                            resources?.getString(R.string.dash_delivery_bold) + "\t" + resources?.getString(
                                R.string.no_timeslots_available_title
                            )
                        } else {
                            resources?.getString(R.string.dash_delivery_bold)
                                .plus("\t" + timeSlot)
                        }


                    deliveryDetailsState.value.copy(
                        icon = R.drawable.ic_dash_delivery_circle,
                        deliveryType = timeSlot,
                        deliveryLocation = WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.address1
                            ?: ""
                    )
                }

                else -> {
                    deliveryDetailsState.value.copy(
                        icon = R.drawable.ic_delivery_circle,
                        deliveryType = resources.getString(R.string.standard_delivery),
                        deliveryLocation = it?.address?.address1 ?: ""
                    )
                }
            }
        }
    }
}