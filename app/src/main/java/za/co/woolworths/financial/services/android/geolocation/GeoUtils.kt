package za.co.woolworths.financial.services.android.geolocation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
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

        fun showFirstFourLocationInMap(addressStorList: List<Store>?, googleMap: GoogleMap?, context: Context) {
            addressStorList?.let {
                for (i in 0..3) {
                    googleMap?.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                addressStorList?.get(i)?.latitude!!,
                                addressStorList?.get(i)?.longitude!!
                            )
                        ).icon(BitmapFromVector(context, R.drawable.pin))
                    )
                    googleMap?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                        addressStorList.get(i)?.latitude!!,
                        addressStorList.get(i)?.longitude!!
                    ), 11f))
                }
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
