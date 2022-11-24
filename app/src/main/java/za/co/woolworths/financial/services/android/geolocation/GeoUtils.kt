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
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapView
import za.co.woolworths.financial.services.android.util.KotlinUtils
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
                if (it.storeId == storeId) {
                    return it
                }
            }
            return null
        }

        fun showFirstFourLocationInMap(addressStoreList: List<Store>?, dynamicMapView: DynamicMapView?, context: Context?) {
            addressStoreList?.let {
                val size = when {
                    addressStoreList.size > 4-> {
                        4
                    }
                    else -> {
                        addressStoreList.size
                    }
                }
                for (i in 0..size) {
                    if (!addressStoreList?.getOrNull(i)?.locationId.isNullOrEmpty()) {
                        dynamicMapView?.addMarker(
                            context!!,
                            latitude = addressStoreList?.getOrNull(i)?.latitude,
                            longitude = addressStoreList?.getOrNull(i)?.longitude,
                            icon = R.drawable.pargopin
                        )
                    } else {
                        dynamicMapView?.addMarker(
                           context!!,
                            latitude = addressStoreList?.getOrNull(i)?.latitude,
                            longitude = addressStoreList?.getOrNull(i)?.longitude,
                            icon = R.drawable.pin
                        )
                    }
                }
            }
            //after plotting all the markers pointing the camera to nearest store
            val store: Store? = addressStoreList?.get(0)
            store?.let {
                dynamicMapView?.moveCamera(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    zoom = 11f
                )
            }
        }

        private fun BitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
            val vectorDrawable: Drawable? = ContextCompat.getDrawable(context, vectorResId)
            vectorDrawable?.apply {
                setBounds(
                    0,
                    0,
                    vectorDrawable.intrinsicWidth,
                    vectorDrawable.intrinsicHeight
                )
            }

            val bitmap: Bitmap? = vectorDrawable?.intrinsicWidth?.let {
                Bitmap.createBitmap(
                    it,
                    vectorDrawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
            }
            val canvas = bitmap?.let { Canvas(it) }
            if (canvas != null) {
                vectorDrawable?.draw(canvas)
            }
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}
