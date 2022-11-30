package za.co.woolworths.financial.services.android.geolocation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.geolocation.network.model.PlaceDetails
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapView
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.StoreUtils
import za.co.woolworths.financial.services.android.util.Utils

class GeoUtils {

    companion object {

        fun getPlaceId() = Utils
            .getPreferredDeliveryLocation()?.fulfillmentDetails?.let {
                it.address?.placeId
            }

        fun getDelivertyType() = KotlinUtils.getPreferredDeliveryType()

        fun getSelectedPlaceId(savedAddresses: SavedAddressResponse): String {
            savedAddresses.addresses?.forEach { address ->
                if (savedAddresses.defaultAddressNickname.equals(address.nickname)) {
                   return address.placesId.toString()
                }
            }
            return  ""
        }

        fun getSelectedDefaultName(
            savedAddresses: SavedAddressResponse?,
            selectedAddressPosition: Int,
        ): Boolean {
            if (savedAddresses?.addresses?.getOrNull(selectedAddressPosition)?.nickname.equals(savedAddresses?.defaultAddressNickname,true)
            ) {
                return true
            }
            return false
        }

        fun getStoreDetails(storeId: String?, stores: List<Store>?): Store? {
            stores?.forEach {

                if(it?.locationId != "" && it?.storeName?.contains(StoreUtils.PARGO, true) == false) {
                    it.storeName = StoreUtils.pargoStoreName(it?.storeName)
                }

                if (it.storeId == storeId) {
                    return it
                }
            }
            return null
        }

        fun showFirstFourLocationInMap(
            addressStoreList: List<Store>?,
            placeDetails: PlaceDetails?,
            dynamicMapView: DynamicMapView?, context: Context?
        ) {
            addressStoreList?.let {
                it.forEachIndexed { position, store ->
                    if (position <= 4) {
                        if (context != null) {
                            dynamicMapView?.addMarker(
                                context,
                                latitude = store?.latitude,
                                longitude = store?.longitude,
                                icon = if (!store.locationId.isNullOrEmpty()) R.drawable.pargopin else R.drawable.pin
                            )
                        }
                        if(position==0) {
                            dynamicMapView?.moveCamera(
                                store?.latitude,
                                store?.longitude,
                                11f
                            )
                        }
                    }
                    else
                        return@forEachIndexed
                }
            }
            placeDetails?.let {
                context?.let {
                    dynamicMapView?.addMarker(
                        context,
                        latitude = placeDetails.latitude,
                        longitude = placeDetails.longitude,
                        icon = R.drawable.cur_address_pin
                    )
                }
            }
        }
    }
}
